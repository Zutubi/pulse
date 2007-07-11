package com.zutubi.pulse.scm.svn;

import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.filesystem.remote.RemoteFile;
import com.zutubi.pulse.scm.*;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.util.StringUtils;
import com.zutubi.pulse.util.logging.Logger;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.*;

import java.io.*;
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

    /**
     * A list of paths within the main repository for which we will check the
     * svn:externals property.  If any externals are set to paths within the
     * *same* repository, they will be taken into account in checkout, update
     * and change checking operations.
     */
    private List<String> externalsPaths = new LinkedList<String>();
    private boolean verifyExternals = true;
    private List<String> excludedPaths;
    private SVNRepository repository;
    private ISVNAuthenticationManager authenticationManager;
    private String uid;
    private String url;

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
            case'A':
                return Change.Action.ADD;
            case'D':
                return Change.Action.DELETE;
            case'M':
                return Change.Action.EDIT;
            case'R':
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

        this.url = url;

        try
        {
            repository = SVNRepositoryFactory.create(SVNURL.parseURIDecoded(url));
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
     * Creates a new SVNServer using the given location and default credentials.
     *
     * @param url
     */
    public SVNServer(String url) throws SCMException
    {
        authenticationManager = SVNWCUtil.createDefaultAuthenticationManager();
        initialiseRepository(url);
    }

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
        authenticationManager.setAuthenticationProvider(new SVNSSHAuthenticationProvider(username, privateKeyFile, null));
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
        authenticationManager.setAuthenticationProvider(new SVNSSHAuthenticationProvider(username, privateKeyFile, passphrase));
        initialiseRepository(url);
    }

    public void setExcludedPaths(List<String> excludedPaths)
    {
        this.excludedPaths = excludedPaths;
    }

    public void addExternalPath(String path)
    {
        externalsPaths.add(path);
    }

    public void setVerifyExternals(boolean verifyExternals)
    {
        this.verifyExternals = verifyExternals;
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
        if (uid == null)
        {
            try
            {
                uid = repository.getRepositoryUUID(true);
            }
            catch (SVNException e)
            {
                throw convertException(e);
            }
        }

        return uid;
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

    public Revision checkout(String id, File toDirectory, Revision revision, SCMCheckoutEventHandler handler) throws SCMException
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

        if (handler != null)
        {
            updateClient.setEventHandler(new ChangeEventHandler(handler));
        }

        try
        {
            updateClient.doCheckout(repository.getLocation(), toDirectory, svnRevision, svnRevision, true);
            updateExternals(toDirectory, revision, updateClient, handler);
        }
        catch (SVNException e)
        {
            throw convertException(e);
        }

        return new NumericalRevision(svnRevision.getNumber());
    }

    private void updateExternals(File toDirectory, Revision revision, SVNUpdateClient client, SCMCheckoutEventHandler handler) throws SCMException
    {
        List<ExternalDefinition> externals = getExternals(revision);
        for (ExternalDefinition external : externals)
        {
            if (handler != null)
            {
                handler.status("Processing external '" + external.path + "'");
            }

            update(new File(toDirectory, FileSystemUtils.denormaliseSeparators(external.path)), convertRevision(revision), client);

            if (handler != null)
            {
                handler.status("External updated to revision " + revision.getRevisionString());
            }
        }
    }

    public String checkout(Revision revision, String file) throws SCMException
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

    private boolean reportChanges(ChangeHandler handler, Revision from, Revision to, String... paths) throws SCMException
    {
        if (to == null)
        {
            to = getLatestRevision();
        }

        long fromNumber = convertRevision(from).getNumber() + 1;
        long toNumber = convertRevision(to).getNumber();

        if (fromNumber <= toNumber)
        {
            try
            {
                if (log(repository, fromNumber, toNumber, handler, paths))
                {
                    return true;
                }

                List<ExternalDefinition> externals = getExternals(to);
                for (ExternalDefinition external : externals)
                {
                    SVNRepository repo = SVNRepositoryFactory.create(external.url);
                    repo.setAuthenticationManager(authenticationManager);
                    if (log(repo, fromNumber, toNumber, handler, paths))
                    {
                        return true;
                    }
                }
            }
            catch (SVNException e)
            {
                throw convertException(e);
            }
        }

        handler.complete();
        return false;
    }

    private boolean log(SVNRepository repository, long fromNumber, long toNumber, ChangeHandler handler, String... paths) throws SVNException, SCMException
    {
        List<SVNLogEntry> logs = new LinkedList<SVNLogEntry>();
        FilepathFilter filter = new ScmFilepathFilter(excludedPaths);

        repository.log(paths, logs, fromNumber, toNumber, true, true);
        for (SVNLogEntry entry : logs)
        {
            NumericalRevision revision = new NumericalRevision(entry.getAuthor(), entry.getMessage(), entry.getDate(), entry.getRevision());

            Changelist list = new Changelist(getUid(), revision);
            handler.handle(list);

            FileRevision fileRevision = new NumericalFileRevision(((NumericalRevision) list.getRevision()).getRevisionNumber());

            Map files = entry.getChangedPaths();

            for (Object value : files.values())
            {
                SVNLogEntryPath entryPath = (SVNLogEntryPath) value;
                if (filter.accept(entryPath.getPath()))
                {
                    if (handler.handle(new Change(entryPath.getPath(), fileRevision, decodeAction(entryPath.getType()))))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    List<ExternalDefinition> getExternals(Revision revision) throws SCMException
    {
        List<ExternalDefinition> result = new LinkedList<ExternalDefinition>();
        if (externalsPaths.size() > 0)
        {
            try
            {
                SVNWCClient wcClient = new SVNWCClient(repository.getAuthenticationManager(), null);
                for (String externalPath : externalsPaths)
                {
                    SVNURL url = repository.getLocation().appendPath(externalPath, false);
                    SVNPropertyData data = wcClient.doGetProperty(url, SVNProperty.EXTERNALS, SVNRevision.HEAD, convertRevision(revision), false);
                    addExternalsFromProperty(StringUtils.join("/", true, true, externalPath, data.getValue()), result);
                }
            }
            catch (IOException e)
            {
                throw new SCMException("I/O error checking externals: " + e.getMessage(), e);
            }
            catch (SVNException e)
            {
                throw convertException(e);
            }
        }

        return result;
    }

    private void addExternalsFromProperty(String value, List<ExternalDefinition> externals) throws IOException, SVNException
    {
        BufferedReader reader = new BufferedReader(new StringReader(value));
        String line;
        while ((line = reader.readLine()) != null)
        {
            line = line.trim();
            if (line.length() == 0 || line.startsWith("#"))
            {
                continue;
            }

            String[] parts = line.split("\\s+");
            if (parts.length == 2)
            {
                // Restriciting to 2 parts means we ignore fixed revision
                // externals (as intended).
                ExternalDefinition external = new ExternalDefinition(parts[0], parts[1]);

                if (verifyExternals)
                {
                    SVNRepository repo = SVNRepositoryFactory.create(external.url);
                    repo.setAuthenticationManager(authenticationManager);

                    try
                    {
                        String uid = repo.getRepositoryUUID(true);
                        if (uid.equals(getUid()))
                        {
                            externals.add(external);
                        }
                        else
                        {
                            LOG.warning("Ignoring external at URL '" + external.url.toDecodedString() + "': UID does not match");
                        }
                    }
                    catch (Exception e)
                    {
                        LOG.warning("Ignoring external at URL '" + external.url.toDecodedString() + "'", e);
                    }
                }
                else
                {
                    externals.add(external);
                }
            }
        }
    }

    public List<Changelist> getChanges(Revision from, Revision to, String... paths) throws SCMException
    {
        ChangelistAccumulator accumulator = new ChangelistAccumulator();
        reportChanges(accumulator, from, to, paths);
        return accumulator.getChangelists();
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
        for (Changelist change : changes)
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
            ChangeDetector detector = new ChangeDetector();
            reportChanges(detector, since, latestRevision, "");
            return detector.isChanged();
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

    public void update(String id, File workDir, Revision rev, SCMCheckoutEventHandler handler) throws SCMException
    {
        // CIB-610: cleanup before update in case WC is locked.
        SVNWCClient wcClient = new SVNWCClient(authenticationManager, null);
        try
        {
            wcClient.doCleanup(workDir);
        }
        catch (SVNException e)
        {
            throw convertException(e);
        }

        SVNUpdateClient client = new SVNUpdateClient(authenticationManager, null);
        if (handler != null)
        {
            client.setEventHandler(new ChangeEventHandler(handler));
        }

        update(workDir, convertRevision(rev), client);
        updateExternals(workDir, rev, client, handler);
    }

    private void update(File workDir, SVNRevision rev, SVNUpdateClient client) throws SCMException
    {
        try
        {
            client.doUpdate(workDir, rev, true);
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

    boolean pathExists(Revision revision, SVNURL path) throws SVNException
    {
        SVNRepository repo = SVNRepositoryFactory.create(path);
        repo.setAuthenticationManager(authenticationManager);
        repo.testConnection();
        SVNDirEntry dir;
        try
        {
            dir = repo.info("", SVNRevision.HEAD.getNumber());
            return dir != null;
        }
        catch (SVNException e)
        {
            return false;
        }
    }

    public void tag(Revision revision, String name, boolean moveExisting) throws SCMException
    {
        try
        {
            SVNURL svnUrl = SVNURL.parseURIDecoded(name);

            if (pathExists(revision, svnUrl))
            {
                if (moveExisting)
                {
                    // Delete existing path
                    SVNCommitClient commitClient = new SVNCommitClient(authenticationManager, null);
                    commitClient.doDelete(new SVNURL[] { svnUrl }, "[pulse] deleting old tag");
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

    public List<ResourceProperty> getConnectionProperties(String id, File dir) throws SCMException
    {
        List<ResourceProperty> properties = new LinkedList<ResourceProperty>();
        properties.add(new ResourceProperty("svn.url", url));
        return properties;
    }

    public void writeConnectionDetails(File outputDir) throws SCMException, IOException
    {
        Properties props = new Properties();
        props.put("location", getLocation());

        FileOutputStream os = null;
        try
        {
            os = new FileOutputStream(new File(outputDir, "svn.properties"));
            props.store(os, "Subversion connection properties");
        }
        finally
        {
            IOUtils.close(os);
        }
    }

    public FileStatus.EOLStyle getEOLPolicy()
    {
        return FileStatus.EOLStyle.BINARY;
    }

    public FileRevision getFileRevision(String path, Revision repoRevision)
    {
        // Subversion does not distinguish between file and repo revisions
        return new NumericalFileRevision(((NumericalRevision) repoRevision).getRevisionNumber());
    }

    public Revision getRevision(String revision) throws SCMException
    {
        try
        {
            long revisionNumber = Long.parseLong(revision);
            if(revisionNumber > repository.getLatestRevision())
            {
                throw new SCMException("Revision '" + revision + "' does not exist in this repository");
            }

            return new NumericalRevision(revisionNumber);
        }
        catch(NumberFormatException e)
        {
            throw new SCMException("Invalid revision '" + revision + ": must be a valid revision number");
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

    private static class ChangeEventHandler implements ISVNEventHandler
    {
        private SCMCheckoutEventHandler handler;

        public ChangeEventHandler(SCMCheckoutEventHandler handler)
        {
            this.handler = handler;
        }

        public void handleEvent(SVNEvent event, double progress)
        {
            Change.Action action = null;

            SVNEventAction svnAction = event.getAction();
            if (svnAction == SVNEventAction.UPDATE_ADD)
            {
                action = Change.Action.ADD;
            }
            else if (svnAction == SVNEventAction.UPDATE_DELETE)
            {
                action = Change.Action.DELETE;
            }
            else if (svnAction == SVNEventAction.UPDATE_UPDATE)
            {
                action = Change.Action.EDIT;
            }

            if (action != null)
            {
                handler.fileCheckedOut(new Change(event.getPath(), null, action));
            }
        }

        public void checkCancelled() throws SVNCancelException
        {
            try
            {
                handler.checkCancelled();
            }
            catch (SCMCancelledException e)
            {
                throw new SVNCancelException();
            }
        }
    }

    private interface ChangeHandler
    {
        void handle(Changelist list);

        boolean handle(Change change);

        void complete();
    }

    private class ChangelistAccumulator implements ChangeHandler
    {
        private List<Changelist> changelists = new LinkedList<Changelist>();
        private Changelist current;

        public List<Changelist> getChangelists()
        {
            return changelists;
        }

        public void handle(Changelist list)
        {
            checkCurrent();
            current = list;
        }

        public boolean handle(Change change)
        {
            current.addChange(change);
            return false;
        }

        public void complete()
        {
            checkCurrent();
        }

        private void checkCurrent()
        {
            if (current != null && current.getChanges().size() > 0)
            {
                String currentRevision = current.getRevision().getRevisionString();
                for (Changelist list : changelists)
                {
                    if (list.getRevision().getRevisionString().equals(currentRevision))
                    {
                        // We have already seen this log entry in another external
                        return;
                    }
                }

                changelists.add(current);
            }
        }
    }

    private class ChangeDetector implements ChangeHandler
    {
        private boolean changed = false;

        public boolean isChanged()
        {
            return changed;
        }

        public void handle(Changelist list)
        {
        }

        public boolean handle(Change change)
        {
            changed = true;
            return true;
        }

        public void complete()
        {
        }
    }

    class ExternalDefinition
    {
        String path;
        SVNURL url;

        public ExternalDefinition(String path, String url) throws SVNException
        {
            this.path = path;
            this.url = SVNURL.parseURIDecoded(url);
        }
    }
}
