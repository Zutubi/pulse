package com.zutubi.pulse.scm.svn;

import com.zutubi.pulse.core.model.NumericalRevision;
import com.zutubi.pulse.personal.PersonalBuildSupport;
import com.zutubi.pulse.scm.FileStatus;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.WorkingCopy;
import com.zutubi.pulse.scm.WorkingCopyStatus;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.wc.*;

import java.io.File;
import java.util.Properties;

/**
 */
public class SvnWorkingCopy extends PersonalBuildSupport implements WorkingCopy
{
    public static final String PROPERTY_URL = "svn.url";

    private File base;
    private SVNClientManager clientManager;

    static
    {
        // Initialise SVN library
        DAVRepositoryFactory.setup();
        SVNRepositoryFactoryImpl.setup();
    }

    public SvnWorkingCopy(File path)
    {
        this.base = path;
        ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
        clientManager = SVNClientManager.newInstance(options);
    }

    public SvnWorkingCopy(File path, String name, String password)
    {
        this.base = path;
        ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
        clientManager = SVNClientManager.newInstance(options, name, password);
    }

    public boolean matchesRepository(Properties repositoryDetails) throws SCMException
    {
        // We just check that the URL matches
        String url = repositoryDetails.getProperty(PROPERTY_URL);
        if(url == null)
        {
            throw new SCMException("Subversion repository details not returned by Pulse server");
        }

        try
        {
            SVNInfo info = clientManager.getWCClient().doInfo(base, null);
            String wcUrl = info.getURL().toString();
            if(wcUrl.equals(url))
            {
                return true;
            }
            else
            {
                warning("Working copy's repository URL '" + wcUrl + "' does not match Pulse project's repository URL '" + url + "'");
                return false;
            }
        }
        catch (SVNException e)
        {
            throw convertException(e);
        }
    }

    public WorkingCopyStatus getStatus() throws SCMException
    {
        StatusHandler handler = new StatusHandler();

        try
        {
            SVNStatusClient statusClient = clientManager.getStatusClient();
            statusClient.setEventHandler(handler);
            statusClient.doStatus(base, true, true, true, false, false, handler);
        }
        catch (SVNException e)
        {
            throw convertException(e);
        }

        return handler.getStatus();
    }

    public void update() throws SCMException
    {
        SVNUpdateClient updateClient = clientManager.getUpdateClient();
        try
        {
            updateClient.doUpdate(base, SVNRevision.HEAD, true);
        }
        catch (SVNException e)
        {
            throw convertException(e);
        }
    }

    private SCMException convertException(SVNException e)
    {
        return new SCMException(e.getMessage(), e);
    }

    private class StatusHandler implements ISVNEventHandler, ISVNStatusHandler
    {
        WorkingCopyStatus status = new WorkingCopyStatus();

        public void handleEvent(SVNEvent event, double progress)
        {
            SVNEventAction action = event.getAction();
            if(action == SVNEventAction.STATUS_COMPLETED)
            {
                // This is the repository revision that the status was run
                // against.  As we check for out of date files against this
                // revision, if no files are out of date then it is safe to
                // check out this revision.  (Note even files we don't have,
                // such as newly-added files, will be reported by the status
                // operation as out of date.)
                status.setRevision(new NumericalRevision(event.getRevision()));
            }
        }

        public void checkCancelled() throws SVNCancelException
        {
        }

        public void handleStatus(SVNStatus svnStatus)
        {
            SVNStatusType contentsStatus = svnStatus.getContentsStatus();
            String path = svnStatus.getFile().getPath();
            boolean directory = svnStatus.getKind() == SVNNodeKind.DIR;

            if(path.startsWith(base.getPath()))
            {
                path = path.substring(base.getPath().length());
            }

            if(path.startsWith("/") || path.startsWith(File.separator))
            {
                path = path.substring(1);
            }

            FileStatus.State fileState = null;

            if(contentsStatus == SVNStatusType.STATUS_NORMAL)
            {
                fileState = FileStatus.State.UNCHANGED;
            }
            else if(contentsStatus == SVNStatusType.STATUS_ADDED)
            {
                fileState = FileStatus.State.ADDED;
            }
            else if(contentsStatus == SVNStatusType.STATUS_CONFLICTED)
            {
                fileState = FileStatus.State.UNRESOLVED;
            }
            else if(contentsStatus == SVNStatusType.STATUS_DELETED)
            {
                fileState = FileStatus.State.DELETED;
            }
            else if(contentsStatus == SVNStatusType.STATUS_EXTERNAL)
            {
                fileState = FileStatus.State.UNSUPPORTED;
            }
            else if(contentsStatus == SVNStatusType.STATUS_INCOMPLETE)
            {
                fileState = FileStatus.State.INCOMPLETE;
            }
            else if(contentsStatus == SVNStatusType.STATUS_MERGED)
            {
                fileState = FileStatus.State.MERGED;
            }
            else if(contentsStatus == SVNStatusType.STATUS_MISSING)
            {
                fileState = FileStatus.State.MISSING;
            }
            else if(contentsStatus == SVNStatusType.STATUS_MODIFIED)
            {
                fileState = FileStatus.State.MODIFIED;
            }
            else if(contentsStatus == SVNStatusType.STATUS_OBSTRUCTED)
            {
                fileState = FileStatus.State.OBSTRUCTED;
            }
            else if(contentsStatus == SVNStatusType.STATUS_REPLACED)
            {
                fileState = FileStatus.State.REPLACED;
            }
            else if(contentsStatus == SVNStatusType.STATUS_NONE)
            {
                fileState = FileStatus.State.UNCHANGED;
            }

            if(fileState != null)
            {
                FileStatus fs = new FileStatus(path, fileState, directory);

                if(svnStatus.getRemoteContentsStatus() != SVNStatusType.STATUS_NONE ||
                   svnStatus.getRemotePropertiesStatus() != SVNStatusType.STATUS_NONE)
                {
                    // Remote change to this file
                    fs.setOutOfDate(true);
                }

                status.add(fs);
            }
        }

        public WorkingCopyStatus getStatus()
        {
            return status;
        }
    }
}
