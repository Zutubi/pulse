package com.zutubi.pulse.scm.cvs;

import com.zutubi.pulse.personal.PersonalBuildSupport;
import com.zutubi.pulse.scm.WorkingCopy;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.WorkingCopyStatus;
import com.zutubi.pulse.scm.FileStatus;

import java.util.Properties;
import java.util.List;
import java.io.File;
import java.io.IOException;

import com.zutubi.pulse.scm.cvs.client.CvsClient;
import com.zutubi.pulse.config.Config;
import com.zutubi.pulse.config.ConfigSupport;
import com.zutubi.pulse.core.model.CvsRevision;
import com.zutubi.pulse.util.IOUtils;
import org.netbeans.lib.cvsclient.CVSRoot;
import static org.netbeans.lib.cvsclient.file.FileStatus.UP_TO_DATE;
import static org.netbeans.lib.cvsclient.file.FileStatus.ADDED;
import static org.netbeans.lib.cvsclient.file.FileStatus.REMOVED;
import static org.netbeans.lib.cvsclient.file.FileStatus.MODIFIED;
import static org.netbeans.lib.cvsclient.file.FileStatus.NEEDS_CHECKOUT;
import static org.netbeans.lib.cvsclient.file.FileStatus.NEEDS_MERGE;
import static org.netbeans.lib.cvsclient.file.FileStatus.NEEDS_PATCH;
import static org.netbeans.lib.cvsclient.file.FileStatus.HAS_CONFLICTS;
import static org.netbeans.lib.cvsclient.file.FileStatus.UNRESOLVED_CONFLICT;
import static org.netbeans.lib.cvsclient.file.FileStatus.UNKNOWN;
import static org.netbeans.lib.cvsclient.file.FileStatus.INVALID;
import org.netbeans.lib.cvsclient.command.status.StatusInformation;

/**
 * <class comment/>
 */
public class CvsWorkingCopy extends PersonalBuildSupport implements WorkingCopy
{
    private CvsClient client;

    private File workingDir;
    private ConfigSupport configSupport;

    public CvsWorkingCopy(File path, Config config)
    {
        this.workingDir = path;
        this.client = new CvsClient();
        this.configSupport = new ConfigSupport(config);

        client.setRoot(CVSRoot.parse(configSupport.getProperty(CvsConstants.ROOT)));
        if (configSupport.hasProperty(CvsConstants.PASS))
        {
            client.setPassword(configSupport.getProperty(CvsConstants.PASS));
        }
    }

    public boolean matchesRepository(Properties repositoryDetails) throws SCMException
    {
        try
        {
            File rootFile = new File(workingDir, "CVS" + File.separator + "Root");
            String localRoot = IOUtils.fileToString(rootFile);
            CVSRoot localCvsRoot = CVSRoot.parse(localRoot);

            String projectRoot = repositoryDetails.getProperty(CvsConstants.ROOT);
            CVSRoot projectCvsRoot = CVSRoot.parse(projectRoot);

            if (localCvsRoot.getCompatibilityLevel(projectCvsRoot) == -1)
            {
                return false;
            }

            // now check that the modules match.
            File repositoryFile = new File(workingDir, "CVS" + File.separator + "Repository");
            String localModule = IOUtils.fileToString(repositoryFile);
            String projectModule = repositoryDetails.getProperty(CvsConstants.MODULE);

            return localModule.equals(projectModule);
        }
        catch (IOException e)
        {
            throw new SCMException(e);
        }
    }

    public WorkingCopyStatus getStatus() throws SCMException
    {
        WorkingCopyStatus status = new WorkingCopyStatus();

        List<StatusInformation> infos = client.status(workingDir);
        for (StatusInformation info : infos)
        {
            processStatusInformation(info, status);
        }

        return status;
    }

    public void update() throws SCMException
    {
        // updating to the latest repository version for the current configurations branch (or head).
        String branch = configSupport.getProperty(CvsConstants.BRANCH);
        if ("".equals(branch))
        {
            // slightly paranoid action. Ensure that we use a null if no branch is specified.
            branch = null;
        }
        
        CvsRevision revision = new CvsRevision(null, branch, null, null);
        client.update(workingDir, revision);
    }

    private void processStatusInformation(StatusInformation fileInfo, WorkingCopyStatus status)
    {
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

        boolean outOfDate = false; // is an update required?

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
            outOfDate = true;
        }
        else if (fileStatus == NEEDS_MERGE)
        {
            fileState = FileStatus.State.MODIFIED;
            outOfDate = true;
        }
        else if (fileStatus == NEEDS_PATCH)
        {
            fileState = FileStatus.State.UNCHANGED;
            outOfDate = true;
        }
        else if (fileStatus == HAS_CONFLICTS)
        {
            fileState = FileStatus.State.MODIFIED;
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
            FileStatus fs = new FileStatus(path, fileState, localFile.isDirectory());
            fs.setOutOfDate(outOfDate);
            if (fs.isInteresting())
            {
                status(fs.toString());
                status.add(fs);
            }
        }

    }
}