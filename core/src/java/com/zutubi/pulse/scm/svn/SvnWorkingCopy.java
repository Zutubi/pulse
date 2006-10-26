package com.zutubi.pulse.scm.svn;

import com.zutubi.pulse.config.Config;
import com.zutubi.pulse.config.ConfigSupport;
import com.zutubi.pulse.core.model.NumericalRevision;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.personal.PersonalBuildSupport;
import com.zutubi.pulse.scm.*;
import static com.zutubi.pulse.scm.svn.SvnConstants.*;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.wc.*;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 */
public class SvnWorkingCopy extends PersonalBuildSupport implements WorkingCopy
{
    private File base;
    private SVNClientManager clientManager;

    static
    {
        // Initialise SVN library
        DAVRepositoryFactory.setup();
        SVNRepositoryFactoryImpl.setup();
    }

    public SvnWorkingCopy(File path, Config config)
    {
        this.base = path;
        ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
        clientManager = SVNClientManager.newInstance(options);

        ConfigSupport configSupport = new ConfigSupport(config);
        if (!configSupport.hasProperty(PROPERTY_KEYFILE))
        {
            if (configSupport.hasProperty(PROPERTY_USERNAME))
            {
                clientManager = SVNClientManager.newInstance(options, configSupport.getProperty(PROPERTY_USERNAME), configSupport.getProperty(PROPERTY_PASSWORD, ""));
            }
            else
            {
                clientManager = SVNClientManager.newInstance(options);
            }
        }
        else
        {
            String username = configSupport.getProperty(PROPERTY_USERNAME);
            String password = configSupport.getProperty(PROPERTY_PASSWORD, "");
            String privateKeyFile = configSupport.getProperty(PROPERTY_KEYFILE);
            String passphrase = configSupport.getProperty(PROPERTY_PASSPHRASE);

            ISVNAuthenticationManager authenticationManager = SVNWCUtil.createDefaultAuthenticationManager(username, password);
            authenticationManager.setAuthenticationProvider(new SVNSSHAuthenticationProvider(username, privateKeyFile, passphrase));
            clientManager = SVNClientManager.newInstance(options, authenticationManager);
        }
    }

    public boolean matchesRepository(Properties repositoryDetails) throws SCMException
    {
        // We just check that the URL matches
        String url = repositoryDetails.getProperty(SvnConstants.PROPERTY_URL);
        if (url == null)
        {
            throw new SCMException("Subversion repository details not returned by Pulse server");
        }

        try
        {
            SVNInfo info = clientManager.getWCClient().doInfo(base, null);
            String wcUrl = info.getURL().toString();
            if (wcUrl.equals(url))
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
        return getStatus(true, base);
    }

    public WorkingCopyStatus getLocalStatus(String... spec) throws SCMException
    {
        File[] files = SCMUtils.specToFiles(base, spec);
        if(files == null)
        {
            return getStatus(false, base);
        }
        else
        {
            return getStatus(false, files);
        }
    }

    private WorkingCopyStatus getStatus(boolean remote, File... files) throws SCMException
    {
        StatusHandler handler = new StatusHandler();

        try
        {
            SVNStatusClient statusClient = clientManager.getStatusClient();
            statusClient.setEventHandler(handler);
            for(File f: files)
            {
                statusClient.doStatus(f, true, remote, true, false, false, handler);
            }

            WorkingCopyStatus wcs = handler.getStatus();

            // Now find out if any changed files have an eol-style
            getProperties(wcs, handler.propertyChangedPaths);

            return wcs;
        }
        catch (SVNException e)
        {
            throw convertException(e);
        }
    }

    private void getProperties(WorkingCopyStatus wcs, List<String> propertyChangedPaths) throws SVNException
    {
        SVNWCClient wcc = clientManager.getWCClient();

        for(FileStatus fs: wcs)
        {
            if(fs.getState().requiresFile())
            {
                SVNPropertyData property = wcc.doGetProperty(new File(base, fs.getPath()), SVN_PROPERTY_EOL_STYLE, SVNRevision.WORKING, SVNRevision.WORKING, false);
                if(property != null)
                {
                    fs.setProperty(FileStatus.PROPERTY_EOL_STYLE, convertEOLStyle(property.getValue()));
                }
            }

            if(fs.getState() == FileStatus.State.ADDED)
            {
                // For new files, check for svn:executable 
                SVNPropertyData property = wcc.doGetProperty(new File(base, fs.getPath()), SVN_PROPERTY_EXECUTABLE, SVNRevision.WORKING, SVNRevision.WORKING, false);
                if(property != null)
                {
                    fs.setProperty(FileStatus.PROPERTY_EXECUTABLE, "true");
                }
            }
        }

        // For items with changed properties, check if the executable property has flipped
        for(String path: propertyChangedPaths)
        {
            FileStatus fs = wcs.getFileStatus(path);
            SVNPropertyData baseProperty = wcc.doGetProperty(new File(base, path), SVN_PROPERTY_EXECUTABLE, SVNRevision.BASE, SVNRevision.BASE, false);
            SVNPropertyData workingProperty = wcc.doGetProperty(new File(base, path), SVN_PROPERTY_EXECUTABLE, SVNRevision.WORKING, SVNRevision.WORKING, false);

            if(baseProperty == null)
            {
                if(workingProperty != null)
                {
                    // Added svn:executable
                    fs.setProperty(FileStatus.PROPERTY_EXECUTABLE, "true");
                }
            }
            else
            {
                if(workingProperty == null)
                {
                    // Removed svn:executable
                    fs.setProperty(FileStatus.PROPERTY_EXECUTABLE, "false");
                }
            }
        }
    }

    private String convertEOLStyle(String eol)
    {
        if(eol.equals("native"))
        {
            return FileStatus.EOLStyle.NATIVE.toString();
        }
        else if(eol.equals("CR"))
        {
            return FileStatus.EOLStyle.CARRIAGE_RETURN.toString();
        }
        else if(eol.equals("CRLF"))
        {
            return FileStatus.EOLStyle.CARRIAGE_RETURN_LINEFEED.toString();
        }
        else if(eol.equals("LF"))
        {
            return FileStatus.EOLStyle.LINEFEED.toString();
        }
        else
        {
            return FileStatus.EOLStyle.BINARY.toString();
        }
    }

    public Revision update() throws SCMException
    {
        SVNUpdateClient updateClient = clientManager.getUpdateClient();
        updateClient.setEventHandler(new UpdateHandler());
        
        try
        {
            long rev = updateClient.doUpdate(base, SVNRevision.HEAD, true);
            return new NumericalRevision(rev);
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

    private FileStatus convertStatus(SVNStatus svnStatus, List<String> propertyChangedPaths)
    {
        SVNStatusType contentsStatus = svnStatus.getContentsStatus();
        String path = svnStatus.getFile().getPath();
        boolean directory = svnStatus.getKind() == SVNNodeKind.DIR;

        if (path.startsWith(base.getPath()))
        {
            path = path.substring(base.getPath().length());
        }

        if (path.startsWith("/") || path.startsWith(File.separator))
        {
            path = path.substring(1);
        }

        FileStatus.State fileState;

        if (contentsStatus == SVNStatusType.STATUS_NORMAL)
        {
            fileState = FileStatus.State.UNCHANGED;
        }
        else if (contentsStatus == SVNStatusType.STATUS_ADDED)
        {
            fileState = FileStatus.State.ADDED;
        }
        else if (contentsStatus == SVNStatusType.STATUS_CONFLICTED)
        {
            fileState = FileStatus.State.UNRESOLVED;
        }
        else if (contentsStatus == SVNStatusType.STATUS_DELETED)
        {
            fileState = FileStatus.State.DELETED;
        }
        else if (contentsStatus == SVNStatusType.STATUS_EXTERNAL)
        {
            fileState = FileStatus.State.UNSUPPORTED;
        }
        else if (contentsStatus == SVNStatusType.STATUS_INCOMPLETE)
        {
            fileState = FileStatus.State.INCOMPLETE;
        }
        else if (contentsStatus == SVNStatusType.STATUS_MERGED)
        {
            fileState = FileStatus.State.MERGED;
        }
        else if (contentsStatus == SVNStatusType.STATUS_MISSING)
        {
            fileState = FileStatus.State.MISSING;
        }
        else if (contentsStatus == SVNStatusType.STATUS_MODIFIED)
        {
            fileState = FileStatus.State.MODIFIED;
        }
        else if (contentsStatus == SVNStatusType.STATUS_OBSTRUCTED)
        {
            fileState = FileStatus.State.OBSTRUCTED;
        }
        else if (contentsStatus == SVNStatusType.STATUS_REPLACED)
        {
            fileState = FileStatus.State.REPLACED;
        }
        else
        {
            fileState = FileStatus.State.UNCHANGED;
        }

        SVNStatusType propertiesStatus = svnStatus.getPropertiesStatus();
        if(propertiesStatus != SVNStatusType.STATUS_NONE)
        {
            propertyChangedPaths.add(path);
        }

        return new FileStatus(path, fileState, directory);
    }

    private class StatusHandler implements ISVNEventHandler, ISVNStatusHandler
    {
        WorkingCopyStatus status = new WorkingCopyStatus();
        List<String> propertyChangedPaths = new LinkedList<String>();

        public void handleEvent(SVNEvent event, double progress)
        {
            SVNEventAction action = event.getAction();
            if (action == SVNEventAction.STATUS_COMPLETED)
            {
                // This is the repository revision that the status was run
                // against.  As we check for out of date files against this
                // revision, if no files are out of date then it is safe to
                // check out this revision.  (Note even files we don't have,
                // such as newly-added files, will be reported by the status
                // operation as out of date.)
                NumericalRevision rev = new NumericalRevision(event.getRevision());
                status.setRevision(rev);
                status("Repository revision: " + rev.getRevisionString());
            }
        }

        public void checkCancelled() throws SVNCancelException
        {
        }

        public void handleStatus(SVNStatus svnStatus)
        {
            FileStatus fs = convertStatus(svnStatus, propertyChangedPaths);

            if (svnStatus.getRemoteContentsStatus() != SVNStatusType.STATUS_NONE ||
                svnStatus.getRemotePropertiesStatus() != SVNStatusType.STATUS_NONE)
            {
                // Remote change to this file
                fs.setOutOfDate(true);
            }

            if (fs.isInteresting())
            {
                status(fs.toString());
                status.add(fs);
            }
        }

        public WorkingCopyStatus getStatus()
        {
            return status;
        }
    }

    private class UpdateHandler implements ISVNEventHandler
    {
        public void handleEvent(SVNEvent event, double progress)
        {
            SVNEventAction action = event.getAction();
            String pathChangeType = " ";
            if (action == SVNEventAction.UPDATE_ADD)
            {
                pathChangeType = "A";
            }
            else if (action == SVNEventAction.UPDATE_DELETE)
            {
                pathChangeType = "D";
            }
            else if (action == SVNEventAction.UPDATE_UPDATE)
            {
                // Find out in detail what state the item is in (after  having  been
                // updated).
                SVNStatusType contentsStatus = event.getContentsStatus();
                if (contentsStatus == SVNStatusType.CHANGED)
                {
                    pathChangeType = "U";
                }
                else if (contentsStatus == SVNStatusType.CONFLICTED)
                {
                    pathChangeType = "C";
                }
                else if (contentsStatus == SVNStatusType.MERGED)
                {
                    pathChangeType = "G";
                }
            }
            else if (action == SVNEventAction.UPDATE_EXTERNAL)
            {
                status("Fetching external item into '" + event.getFile().getAbsolutePath() + "'");
                status("External at revision " + event.getRevision());
                return;
            }
            else if (action == SVNEventAction.UPDATE_COMPLETED)
            {
                /*
                * Updating the working copy is completed. Prints out the revision.
                */
                status("Updated to revision " + event.getRevision());
                return;
            }
            else if (action == SVNEventAction.ADD)
            {
                status("A     " + event.getPath());
                return;
            }
            else if (action == SVNEventAction.DELETE)
            {
                status("D     " + event.getPath());
                return;
            }
            else if (action == SVNEventAction.LOCKED)
            {
                status("L     " + event.getPath());
                return;
            }
            else if (action == SVNEventAction.LOCK_FAILED)
            {
                status("Failed to lock: " + event.getPath());
                return;
            }

            // For added, delete or updated files, check the properties
            // status.
            SVNStatusType propertiesStatus = event.getPropertiesStatus();

            String propertiesChangeType = " ";
            if (propertiesStatus == SVNStatusType.CHANGED)
            {
                propertiesChangeType = "U";
            }
            else if (propertiesStatus == SVNStatusType.CONFLICTED)
            {
                propertiesChangeType = "C";
            }
            else if (propertiesStatus == SVNStatusType.MERGED)
            {
                propertiesChangeType = "G";
            }

            // Also get the loack status
            String lockLabel = " ";
            SVNStatusType lockType = event.getLockStatus();

            if (lockType == SVNStatusType.LOCK_UNLOCKED)
            {
                lockLabel = "B";
            }

            status(pathChangeType + propertiesChangeType + lockLabel + "       " + event.getPath());
        }

        /*
        * Should be implemented to check if the current operation is cancelled. If
        * it is, this method should throw an SVNCancelException.
        */
        public void checkCancelled() throws SVNCancelException
        {
        }
    }
}
