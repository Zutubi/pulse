package com.zutubi.pulse.scm.svn;

import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.NumericalRevision;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.filesystem.remote.RemoteFile;
import com.zutubi.pulse.scm.FilepathFilter;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.SCMServer;
import com.zutubi.pulse.scm.ScmFilepathFilter;
import com.zutubi.pulse.util.logging.Logger;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationProvider;
import org.tmatesoft.svn.core.auth.SVNAuthentication;
import org.tmatesoft.svn.core.auth.SVNSSHAuthentication;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.*;

/**
 * A connection to a subversion server.
 *
 * @author jsankey
 */
public class SVNServer implements SCMServer
{
    private static final Logger LOG = Logger.getLogger(SCMServer.class);
    private static final int CHECKOUT_RETRIES = 1;

    private List<String> excludedPaths;
    private SVNRepository repository;
    ISVNAuthenticationManager authenticationManager;

    //=======================================================================
    // Implementation
    //=======================================================================

    /**
     * Converts a subversion exception to a generic SCMException.
     *
     * @param e the exception to convert
     * @return the converted form of the exception
     */
    private SCMException convertException(SVNException e)
    {
        LOG.error(e);
        return new SCMException(e.getMessage(), e);
    }

    /**
     * Converts a generic revision to the subversion revision number.
     *
     * @param revision the revision to convert
     * @return the subversion revision number
     */
    private SVNRevision convertRevision(Revision revision)
    {
        if (revision == null)
        {
            return SVNRevision.HEAD;
        }
        else
        {
            return SVNRevision.create(((NumericalRevision) revision).getRevisionNumber());
        }
    }

    /**
     * Decodes a change action given a character code from subversion.
     *
     * @param type the action type as returned by the server
     * @return the corresponding Action value
     */
    private Change.Action decodeAction(char type)
    {
        switch (type)
        {
            case 'A':
                return Change.Action.ADD;
            case 'D':
                return Change.Action.DELETE;
            case 'M':
                return Change.Action.EDIT;
            case 'R':
                return Change.Action.MOVE;
            default:
                return Change.Action.UNKNOWN;
        }
    }

    /**
     * Initialises a connection to the subversion repository.
     *
     * @param url the URL to connect to the server on
     * @throws SCMException if an error occurs connecting to the server
     */
    private void initialiseRepository(String url) throws SCMException
    {
        // Initialise SVN library
        DAVRepositoryFactory.setup();
        SVNRepositoryFactoryImpl.setup();

        try
        {
            repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
            repository.setAuthenticationManager(authenticationManager);
        }
        catch (SVNException e)
        {
            throw convertException(e);
        }
    }

    /**
     * Helper class for identifying the files added during a checkout.
     */
//    private class ChangeAccumulator implements ISVNWorkspaceListener
//    {
//        public List<Change> changes;
//
//        public ChangeAccumulator(List<Change> changes)
//        {
//            this.changes = changes;
//        }
//
//        public void updated(String path, int contentsStatus, int propertiesStatus, long revision)
//        {
//            changes.add(new Change(path, Long.toString(revision), Change.Action.ADD));
//        }
//
//        public void committed(String path, int kind)
//        {
//        }
//
//        public void modified(String path, int kind)
//        {
//        }
//    }

    //=======================================================================
    // Construction
    //=======================================================================

    /**
     * Creates a new SVNServer using the given location and credentials to
     * connect to the server.
     *
     * @param url      url location of the server and module to use
     * @param username username to provide on connection
     * @param password password for the given user
     * @throws SCMException if a connection cannot be established
     */
    public SVNServer(String url, String username, String password) throws SCMException
    {
        authenticationManager = SVNWCUtil.createDefaultAuthenticationManager(username, password);
        initialiseRepository(url);
    }

    /**
     * Creates a new SVNServer using the given location and credentials to
     * connect to the server.
     *
     * @param url            url location of the server and module to use
     * @param username       username to provide on connection
     * @param password       password for the given user
     * @param privateKeyFile location of the private key to provide on login
     * @throws SCMException if a connection cannot be established
     */
    public SVNServer(String url, final String username, final String password, final String privateKeyFile) throws SCMException
    {
        authenticationManager = SVNWCUtil.createDefaultAuthenticationManager(username, password);
        authenticationManager.setAuthenticationProvider(new ISVNAuthenticationProvider()
        {
            public SVNAuthentication requestClientAuthentication(String kind, SVNURL url, String realm, String errorMessage, SVNAuthentication previousAuth, boolean authMayBeStored)
            {
                return new SVNSSHAuthentication(username, new File(privateKeyFile), null, 22, false);
            }

            public int acceptServerAuthentication(SVNURL url, String realm, Object certificate, boolean resultMayBeStored)
            {
                return ACCEPTED;
            }
        });
        initialiseRepository(url);
    }

    /**
     * Creates a new SVNServer using the given location and credentials to
     * connect to the server.
     *
     * @param url            url location of the server and module to use
     * @param username       username to provide on connection
     * @param password       password for the given user
     * @param privateKeyFile location of the private key to provide on login
     * @param passphrase     passphrase for the given private key file
     * @throws SCMException if a connection cannot be established
     */
    public SVNServer(String url, final String username, final String password, final String privateKeyFile, final String passphrase) throws SCMException
    {
        authenticationManager = SVNWCUtil.createDefaultAuthenticationManager(username, password);
        authenticationManager.setAuthenticationProvider(new ISVNAuthenticationProvider()
        {
            public SVNAuthentication requestClientAuthentication(String kind, SVNURL url, String realm, String errorMessage, SVNAuthentication previousAuth, boolean authMayBeStored)
            {
                return new SVNSSHAuthentication(username, new File(privateKeyFile), passphrase, 22, false);
            }

            public int acceptServerAuthentication(SVNURL url, String realm, Object certificate, boolean resultMayBeStored)
            {
                return ACCEPTED;
            }
        });
        initialiseRepository(url);
    }

    public void setExcludedPaths(List<String> excludedPaths)
    {
        this.excludedPaths = excludedPaths;
    }

    //=======================================================================
    // SCMServer interface
    //=======================================================================

    public Map<String, String> getServerInfo() throws SCMException
    {
        // Unfortunately we can't find out much about the server, we just
        // know where it is
        Map<String, String> info = new TreeMap<String, String>();
        info.put("location", repository.getLocation().toString());
        return info;
    }

    public String getUid() throws SCMException
    {
        String id = repository.getRepositoryUUID();
        if(id == null)
        {
            // We have to connect to the server to get the id.
            testConnection();
            id = repository.getRepositoryUUID();
        }
        return id;
    }

    public String getLocation()
    {
        return repository.getLocation().toString();
    }

    public void testConnection() throws SCMException
    {
        try
        {
            repository.testConnection();
        }
        catch (SVNException e)
        {
            throw new SCMException(e);
        }
    }

    /**
     * @see SCMServer#checkout(long, File, Revision, List<Change>)
     */
    public Revision checkout(long id, File toDirectory, Revision revision, List<Change> changes) throws SCMException
    {
        SVNRevision svnRevision;
        SVNUpdateClient updateClient = new SVNUpdateClient(repository.getAuthenticationManager(), null);

        if (revision == null)
        {
            svnRevision = SVNRevision.HEAD;
        }
        else
        {
            svnRevision = convertRevision(revision);
        }

        try
        {
            updateClient.doCheckout(repository.getLocation(), toDirectory, svnRevision, svnRevision, true);
        }
        catch (SVNException e)
        {
            throw convertException(e);
        }

        return new NumericalRevision(svnRevision.getNumber());
    }

    public String checkout(long id, Revision revision, String file) throws SCMException
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024);

        try
        {
            repository.getFile(file, convertRevision(revision).getNumber(), null, os);
        }
        catch (SVNException e)
        {
            throw convertException(e);
        }

        return os.toString();
    }

    public List<Changelist> getChanges(Revision from, Revision to, String ...paths) throws SCMException
    {
        if(to == null)
        {
            to = getLatestRevision();
        }
        
        List<Changelist>  result = new LinkedList<Changelist>();
        long fromNumber = convertRevision(from).getNumber() + 1;
        long toNumber = convertRevision(to).getNumber();

        if (fromNumber <= toNumber)
        {
            try
            {
                List<SVNLogEntry> logs = new LinkedList<SVNLogEntry>();

                FilepathFilter filter = new ScmFilepathFilter(excludedPaths);

                repository.log(paths, logs, fromNumber, toNumber, true, true);
                for (SVNLogEntry entry : logs)
                {
                    NumericalRevision revision = new NumericalRevision(entry.getRevision());
                    revision.setAuthor(entry.getAuthor());
                    revision.setComment(entry.getMessage());
                    revision.setDate(entry.getDate());
                    // branch??

                    Changelist list = new Changelist(getUid(), revision);
                    Map files = entry.getChangedPaths();

                    for (Object value : files.values())
                    {
                        SVNLogEntryPath entryPath = (SVNLogEntryPath) value;
                        if (filter.accept(entryPath.getPath()))
                        {
                            list.addChange(new Change(entryPath.getPath(), list.getRevision().toString(), decodeAction(entryPath.getType())));
                        }
                    }

                    if (list.getChanges().size() > 0)
                    {
                        result.add(list);
                    }
                }
            }
            catch (SVNException e)
            {
                throw convertException(e);
            }
        }

        return result;
    }

    public List<Revision> getRevisionsSince(Revision from) throws SCMException
    {
        List<Changelist> changes = getChanges(from, null, "");
        Collections.sort(changes, new Comparator<Changelist>()
        {
            public int compare(Changelist o1, Changelist o2)
            {
                return o1.getDate().compareTo(o2.getDate());
            }
        });

        List<Revision> result = new LinkedList<Revision>();
        for(Changelist change: changes)
        {
            result.add(change.getRevision());
        }

        return result;
    }

    public boolean hasChangedSince(Revision since) throws SCMException
    {
        NumericalRevision latestRevision = getLatestRevision();
        if (latestRevision.getRevisionNumber() != ((NumericalRevision) since).getRevisionNumber())
        {
            return getChanges(since, latestRevision, "").size() > 0;
        }
        else
        {
            return false;
        }
    }

    public NumericalRevision getLatestRevision() throws SCMException
    {
        try
        {
            return new NumericalRevision(repository.getLatestRevision());
        }
        catch (SVNException e)
        {
            throw convertException(e);
        }
    }

    public RemoteFile getFile(String path) throws SCMException
    {
        try
        {
            boolean directory = false;

            SVNNodeKind kind = repository.checkPath(path, -1);
            if (kind == SVNNodeKind.DIR)
            {
                directory = true;
            }

            return new RemoteFile(directory, null, path);
        }
        catch (SVNException e)
        {
            throw convertException(e);
        }
    }

    public List<RemoteFile> getListing(String path) throws SCMException
    {
        LinkedList<SVNDirEntry> files = new LinkedList<SVNDirEntry>();
        try
        {
            repository.getDir(path, -1, null, files);
        }
        catch (SVNException e)
        {
            throw convertException(e);
        }

        List<RemoteFile> result = new LinkedList<RemoteFile>();
        String pathPrefix = "";
        if (path.length() > 0)
        {
            pathPrefix = path + "/";
        }

        for (SVNDirEntry e : files)
        {
            boolean isDir;

            if (e.getKind() == SVNNodeKind.DIR)
            {
                isDir = true;
            }
            else if (e.getKind() == SVNNodeKind.FILE)
            {
                isDir = false;
            }
            else
            {
                continue;
            }

            RemoteFile f = new RemoteFile(e.getName(), isDir, null, pathPrefix + e.getName());
            result.add(f);
        }

        return result;
    }

    public void update(File workDir, Revision rev) throws SCMException
    {
        SVNUpdateClient client = new SVNUpdateClient(authenticationManager, null);
        try
        {
            client.doUpdate(workDir, convertRevision(rev), true);
        }
        catch (SVNException e)
        {
            throw convertException(e);
        }
    }

    public boolean supportsUpdate()
    {
        return true;
    }

    public void tag(Revision revision, String name, boolean moveExisting) throws SCMException
    {
        try
        {
            SVNURL svnUrl = SVNURL.parseURIEncoded(name);
            SVNRepository repo = SVNRepositoryFactory.create(svnUrl);
            repo.setAuthenticationManager(authenticationManager);
            SVNDirEntry dir = repo.info("", SVNRevision.HEAD.getNumber());
            if(dir != null)
            {
                if(moveExisting)
                {
                    // Delete existing path
                    SVNCommitClient commitClient = new SVNCommitClient(authenticationManager, null);
                    commitClient.doDelete(new SVNURL[]{ svnUrl }, "[pulse] deleting old tag");
                }
                else
                {
                    throw new SCMException("Unable to apply tag: path '" + name + "' already exists in the repository");
                }
            }

            SVNCopyClient copyClient = new SVNCopyClient(authenticationManager, null);
            copyClient.doCopy(repository.getLocation(), convertRevision(revision), svnUrl, false, "[pulse] applying tag");
        }
        catch (SVNException e)
        {
            throw convertException(e);
        }
    }

    //=======================================================================
    // Testing use only
    //=======================================================================

    public static void main(String argv[])
    {
        try
        {
            SVNServer server = new SVNServer("svn+ssh://jason@www.anyhews.net/usr/local/svn-repo/pulse/trunk", argv[0], argv[1]);
            //server.checkout(new File("/home/jsankey/svntest"), new SVNRevision(ISVNWorkspace.HEAD));
            List<Changelist> cls = server.getChanges(new NumericalRevision(47), new NumericalRevision(-1), "");

            for (Changelist l : cls)
            {
                System.out.println("Changelist:");
                System.out.println("  Revision: " + l.getRevision());
                System.out.println("  Date    : " + l.getDate());
                System.out.println("  User    : " + l.getUser());
                System.out.println("  Comment : " + l.getComment());
                System.out.println("  Files   : " + l.getRevision());

                for (Change c : l.getChanges())
                {
                    System.out.println("    " + c.getFilename() + "#" + c.getRevision() + " - " + c.getAction());
                }
            }
        }
        catch (SCMException e)
        {
            e.printStackTrace();
        }
    }
}
