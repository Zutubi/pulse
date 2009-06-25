package com.zutubi.pulse.core.scm.cvs;

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
import java.io.OutputStream;
import java.util.Date;

/**
 */
public class CvsWorkingCopy implements WorkingCopy, WorkingCopyStatusBuilder
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

    public Revision getLatestRemoteRevision(WorkingCopyContext context) throws ScmException
    {
        // Just fix it to the current time.
        return CvsClient.convertRevision(new CvsRevision(null, getBranch(context), null, new Date()));
    }

    public Revision guessLocalRevision(WorkingCopyContext context) throws ScmException
    {
        throw new ScmException("Operation not supported");
    }

    public Revision update(WorkingCopyContext context, Revision revision) throws ScmException
    {
        if (revision == null)
        {
            revision = getLatestRemoteRevision(context);
        }
        
        getCore(context).update(context.getBase(), CvsClient.convertRevision(revision), new UpdateHandler(context.getUI()));
        return revision;
    }

    private String getBranch(WorkingCopyContext context)
    {
        String branch = context.getConfig().getProperty(CvsConstants.BRANCH);
        if (branch != null && branch.trim().length() == 0)
        {
            // slightly paranoid action. Ensure that we use a null if no branch is specified.
            branch = null;
        }
        return branch;
    }

    public boolean writePatchFile(WorkingCopyContext context, File patchFile, String... scope) throws ScmException
    {
        return StandardPatchFileSupport.writePatchFile(this, context, patchFile, scope);
    }

    public WorkingCopyStatus getLocalStatus(WorkingCopyContext context, String... paths) throws ScmException
    {
        File workingDir = context.getBase();
        File[] files = pathsToFiles(workingDir, paths);
        WorkingCopyStatus status = new WorkingCopyStatus(workingDir);
        StatusHandler statusHandler = new StatusHandler(workingDir, loadLocalWorkingModule(workingDir), status, context.getUI());

        getCore(context).status(workingDir, files, statusHandler);

        return status;
    }

    public boolean canDiff(WorkingCopyContext context, String path) throws ScmException
    {
        return false;
    }

    public void diff(WorkingCopyContext context, String path, OutputStream output) throws ScmException
    {
        getCore(context).diff(context.getBase(), path, output);
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

    private File[] pathsToFiles(File base, String... spec) throws ScmException
    {
        if(spec.length == 0)
        {
            return null;
        }

        File[] result = new File[spec.length];
        for(int i = 0; i < spec.length; i++)
        {
            result[i] = new File(base, spec[i]);
            if(!result[i].exists())
            {
                throw new ScmException("File '" + spec[i] + "' does not exist");
            }
        }

        return result;
    }

    private class UpdateHandler extends CVSAdapter
    {
        private String basePath = new File("").getAbsolutePath();
        private PersonalBuildUI ui;

        public UpdateHandler(PersonalBuildUI ui)
        {
            this.ui = ui;
        }

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

            ui.status(String.format("%s     %s", info.getType(), file));
        }
    }

    private class StatusHandler extends CVSAdapter
    {
        private File workingDir;
        private String localWorkingModule;
        private WorkingCopyStatus status = null;
        private PersonalBuildUI ui;

        public StatusHandler(File workingDir, String localWorkingModule, WorkingCopyStatus status, PersonalBuildUI ui)
        {
            this.workingDir = workingDir;
            this.localWorkingModule = localWorkingModule;
            this.status = status;
            this.ui = ui;
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
                    ui.status(fs.toString());
                    status.addFileStatus(fs);
                }
            }
        }
    }
}