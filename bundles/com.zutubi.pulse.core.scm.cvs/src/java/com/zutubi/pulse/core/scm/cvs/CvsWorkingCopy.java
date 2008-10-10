package com.zutubi.pulse.core.scm.cvs;

import com.zutubi.pulse.core.personal.PersonalBuildUIAwareSupport;
import com.zutubi.pulse.core.scm.ScmUtils;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.cvs.client.CvsCore;
import com.zutubi.util.TextUtils;
import com.zutubi.util.config.Config;
import com.zutubi.util.io.IOUtils;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.command.DefaultFileInfoContainer;
import org.netbeans.lib.cvsclient.command.status.StatusInformation;
import org.netbeans.lib.cvsclient.event.CVSAdapter;
import org.netbeans.lib.cvsclient.event.FileInfoEvent;
import static org.netbeans.lib.cvsclient.file.FileStatus.*;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 */
public class CvsWorkingCopy extends PersonalBuildUIAwareSupport implements WorkingCopy
{
    public boolean matchesLocation(WorkingCopyContext context, String location) throws ScmException
    {
        // Location is <root>[<module>]
        String[] pieces = location.split("\\[");
        pieces[1] = pieces[1].substring(0, pieces[1].length() - 1);

        CVSRoot localCvsRoot = loadLocalWorkingRoot(context);
        CVSRoot projectCvsRoot = CVSRoot.parse(pieces[0]);
        if (localCvsRoot.getCompatibilityLevel(projectCvsRoot) == -1)
        {
            return false;
        }

        // now check that the modules match.
        return loadLocalWorkingModule(context.getBase()).equals(pieces[1]);
    }

    public WorkingCopyStatus getLocalStatus(WorkingCopyContext context, String... spec) throws ScmException
    {
        File workingDir = context.getBase();
        File[] files = ScmUtils.specToFiles(workingDir, spec);
        WorkingCopyStatus status = new WorkingCopyStatus(workingDir);
        StatusHandler statusHandler = new StatusHandler(workingDir, loadLocalWorkingModule(workingDir), status);

        getCore(context).status(workingDir, files, statusHandler);

        return status;
    }

    public Revision update(WorkingCopyContext context, Revision revision) throws ScmException
    {
        // updating to the latest repository version for the current configurations branch (or head).
        String branch = context.getConfig().getProperty(CvsConstants.BRANCH);
        if ("".equals(branch))
        {
            // slightly paranoid action. Ensure that we use a null if no branch is specified.
            branch = null;
        }

        UpdateHandler updateHandler = new UpdateHandler();

        CvsRevision cvsRevision = revision == null ? new CvsRevision(null, branch, null, new Date()) : new CvsRevision(revision.getRevisionString());
        getCore(context).update(context.getBase(), cvsRevision, updateHandler);
        return CvsClient.convertRevision(cvsRevision);
    }

    private String loadLocalWorkingModule(File workingDir) throws ScmException
    {
        try
        {
            File repositoryFile = new File(workingDir, "CVS" + File.separator + "Repository");
            return IOUtils.fileToString(repositoryFile).trim();
        }
        catch (IOException e)
        {
            throw new ScmException("Unable to load local working module: " + e.getMessage(), e);
        }
    }

    private CVSRoot loadLocalWorkingRoot(WorkingCopyContext context) throws ScmException
    {
        File rootFile = new File(context.getBase(), "CVS" + File.separator + "Root");
        String rootString;
        Config config = context.getConfig();
        if (config.hasProperty(CvsConstants.ROOT))
        {
            rootString = config.getProperty(CvsConstants.ROOT);
        }
        else
        {
            try
            {
                rootString = IOUtils.fileToString(rootFile).trim();
            }
            catch (IOException e)
            {
                throw new ScmException("Failed to determine the cvs root. You can work around this problem by adding " +
                        "the cvs.root property to the .pulse2.properties file.");
            }
        }

        try
        {
            return CVSRoot.parse(rootString);
        }
        catch (IllegalArgumentException e)
        {
            throw new ScmException("Invalid CVS root '" + rootString + "': " + e.getMessage(), e);
        }
    }

    private CvsCore getCore(WorkingCopyContext context) throws ScmException
    {
        CvsCore core = new CvsCore();
        CVSRoot cvsRoot = loadLocalWorkingRoot(context);
        if (!TextUtils.stringSet(cvsRoot.getPassword()))
        {
            Config config = context.getConfig();
            if (config.hasProperty(CvsConstants.PASS))
            {
                core.setPassword(config.getProperty(CvsConstants.PASS));
            }
        }

        core.setRoot(cvsRoot);
        return core;
    }

    private class UpdateHandler extends CVSAdapter
    {
        private String basePath = new File("").getAbsolutePath();

        public void fileInfoGenerated(FileInfoEvent e)
        {
            DefaultFileInfoContainer info = (DefaultFileInfoContainer) e.getInfoContainer();

            // determine the path relative to the current working directory.
            String file = info.getFile().getAbsolutePath();
            if (file.startsWith(basePath))
            {
                file = file.substring(basePath.length() + 1);
                if (file.startsWith(File.separator))
                {
                    file = file.substring(1);
                }
            }

            getUI().status(String.format("%s     %s", info.getType(), file));
        }
    }

    private class StatusHandler extends CVSAdapter
    {
        private File workingDir;
        private String localWorkingModule;
        private WorkingCopyStatus status = null;

        public StatusHandler(File workingDir, String localWorkingModule, WorkingCopyStatus status)
        {
            this.workingDir = workingDir;
            this.localWorkingModule = localWorkingModule;
            this.status = status;
        }

        public void fileInfoGenerated(FileInfoEvent e)
        {
            StatusInformation fileInfo = (StatusInformation) e.getInfoContainer();
            File localFile = fileInfo.getFile();

            String path = localFile.getPath();
            if(path.startsWith(workingDir.getPath()))
            {
                path = path.substring(workingDir.getPath().length());
            }

            if(path.startsWith("/") || path.startsWith(File.separator))
            {
                path = path.substring(1);
            }

            FileStatus.State fileState = null;

            org.netbeans.lib.cvsclient.file.FileStatus fileStatus = fileInfo.getStatus();

            if (fileStatus == UP_TO_DATE)
            {
                fileState = FileStatus.State.UNCHANGED;
            }
            else if (fileStatus == ADDED)
            {
                fileState = FileStatus.State.ADDED;
            }
            else if (fileStatus == REMOVED)
            {
                fileState = FileStatus.State.DELETED;
            }
            else if (fileStatus == MODIFIED)
            {
                fileState = FileStatus.State.MODIFIED;
            }
            else if (fileStatus == NEEDS_CHECKOUT)
            {
                fileState = FileStatus.State.UNCHANGED;
            }
            else if (fileStatus == NEEDS_MERGE)
            {
                fileState = FileStatus.State.MODIFIED;
            }
            else if (fileStatus == NEEDS_PATCH)
            {
                fileState = FileStatus.State.UNCHANGED;
            }
            else if (fileStatus == HAS_CONFLICTS)
            {
                fileState = FileStatus.State.UNRESOLVED;
            }
            else if (fileStatus == UNRESOLVED_CONFLICT)
            {
                fileState = FileStatus.State.OBSTRUCTED;
            }
            else if (fileStatus == UNKNOWN)
            {
                // the file is not part of the scm, so it is not returned. This is specific to the working copy status.
                // Directories and files that have not been checked in are considered unknown.
            }
            else if (fileStatus == INVALID)
            {
                fileState = FileStatus.State.UNSUPPORTED;
            }

            if (fileState != null)
            {
                FileStatus fs = new FileStatus(path, fileState, localFile.isDirectory(), localWorkingModule + "/" + path);
                if (fs.isInteresting())
                {
                    getUI().status(fs.toString());
                    status.addFileStatus(fs);
                }
            }
        }
    }
}