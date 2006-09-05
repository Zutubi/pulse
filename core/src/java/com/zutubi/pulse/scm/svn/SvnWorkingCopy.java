package com.zutubi.pulse.scm.svn;

import com.zutubi.pulse.core.model.NumericalRevision;
import com.zutubi.pulse.scm.FileStatus;
import com.zutubi.pulse.scm.WorkingCopy;
import com.zutubi.pulse.scm.WorkingCopyStatus;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.wc.*;

import java.io.File;

/**
 */
public class SvnWorkingCopy implements WorkingCopy
{
    private File base;
    private SVNClientManager clientManager;

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

    public WorkingCopyStatus getStatus()
    {
        StatusHandler handler = new StatusHandler();

        try
        {
            SVNStatusClient statusClient = clientManager.getStatusClient();
            statusClient.doStatus(base, true, false, true, false, false, handler);
        }
        catch (SVNException e)
        {
            e.printStackTrace();
        }

        return handler.getStatus();
    }

    private class StatusHandler implements ISVNStatusHandler
    {
        WorkingCopyStatus status = new WorkingCopyStatus();

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

            if(path.length() == 0)
            {
                // TODO dev-personal: what if nested files are updated to
                // another revision??
                // Grab the revision for the base directory
                status.setRevision(new NumericalRevision(svnStatus.getRevision().getNumber()));
            }

            if(contentsStatus == SVNStatusType.STATUS_ADDED)
            {
                status.add(new FileStatus(path, FileStatus.State.ADDED, directory));
            }
            else if(contentsStatus == SVNStatusType.STATUS_CONFLICTED)
            {
                status.add(new FileStatus(path, FileStatus.State.UNRESOLVED, directory));
            }
            else if(contentsStatus == SVNStatusType.STATUS_DELETED)
            {
                status.add(new FileStatus(path, FileStatus.State.DELETED, directory));
            }
            else if(contentsStatus == SVNStatusType.STATUS_EXTERNAL)
            {
                status.add(new FileStatus(path, FileStatus.State.UNSUPPORTED, directory));
            }
            else if(contentsStatus == SVNStatusType.STATUS_INCOMPLETE)
            {
                status.add(new FileStatus(path, FileStatus.State.INCOMPLETE, directory));
            }
            else if(contentsStatus == SVNStatusType.STATUS_MERGED)
            {
                status.add(new FileStatus(path, FileStatus.State.MERGED, directory));
            }
            else if(contentsStatus == SVNStatusType.STATUS_MISSING)
            {
                status.add(new FileStatus(path, FileStatus.State.MISSING, directory));
            }
            else if(contentsStatus == SVNStatusType.STATUS_MODIFIED)
            {
                status.add(new FileStatus(path, FileStatus.State.MODIFIED, directory));
            }
            else if(contentsStatus == SVNStatusType.STATUS_OBSTRUCTED)
            {
                status.add(new FileStatus(path, FileStatus.State.OBSTRUCTED, directory));
            }
            else if(contentsStatus == SVNStatusType.STATUS_REPLACED)
            {
                status.add(new FileStatus(path, FileStatus.State.REPLACED, directory));
            }
        }

        public WorkingCopyStatus getStatus()
        {
            return status;
        }
    }
}
