package com.zutubi.pulse.core.scm.svn;

import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.engine.api.ResourceProperty;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.StringUtils;
import com.zutubi.util.TextUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.internal.wc.admin.SVNAdminAreaFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.*;

import java.io.*;
import java.util.*;

/**
 * A connection to a subversion server.
 */
public class SubversionClient implements ScmClient
{
    public static final String TYPE = "svn";

    private static final Logger LOG = Logger.getLogger(ScmClient.class);

    /**
     * A list of paths within the main repository for which we will check the
     * svn:externals property.  If any externals are set to paths within the
     * *same* repository, they will be taken into account in checkout, update
     * and change checking operations.
     */
    private List<String> externalsPaths = new LinkedList<String>();
    private boolean verifyExternals = true;
    private List<String> excludedPaths = new LinkedList<String>();
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
    private ScmException convertException(SVNException e)
    {
        LOG.error(e);
        return new ScmException(e.getMessage(), e);
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
            return SVNRevision.create(Long.parseLong(revision.toString()));
        }
    }

    /**
     * Decodes a change action given a character code from subversion.
     *
     * @param type the action type as returned by the server
     * @return the corresponding Action value
     */
    private FileChange.Action decodeAction(char type)
    {
        switch (type)
        {
            case'A':
                return FileChange.Action.ADD;
            case'D':
                return FileChange.Action.DELETE;
            case'M':
                return FileChange.Action.EDIT;
            case'R':
                return FileChange.Action.MOVE;
            default:
                return FileChange.Action.UNKNOWN;
        }
    }

    /**
     * Initialises a connection to the subversion repository.
     *
     * @param url the URL to connect to the server on
     * @throws ScmException if an error occurs connecting to the server
     */
    private void initialiseRepository(String url) throws ScmException
    {
        // Initialise SVN library
        DAVRepositoryFactory.setup();
        SVNRepositoryFactoryImpl.setup();
        FSRepositoryFactory.setup();
        SVNAdminAreaFactory.setUpgradeEnabled(false);
        
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

    //=======================================================================
    // Construction
    //=======================================================================

    /**
     * Creates a new SVNServer using the given location and default credentials.
     *
     * @param url the url of the SVN repository
     * @throws com.zutubi.pulse.core.scm.api.ScmException on error
     */
    public SubversionClient(String url) throws ScmException
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
     * @throws ScmException if a connection cannot be established
     */
    public SubversionClient(String url, String username, String password) throws ScmException
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
     * @throws ScmException if a connection cannot be established
     */
    public SubversionClient(String url, final String username, final String password, final String privateKeyFile) throws ScmException
    {
        authenticationManager = SVNWCUtil.createDefaultAuthenticationManager(null, username, password, new File(privateKeyFile), null, getStoreFlag());
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
     * @throws ScmException if a connection cannot be established
     */
    public SubversionClient(String url, final String username, final String password, final String privateKeyFile, final String passphrase) throws ScmException
    {
        authenticationManager = SVNWCUtil.createDefaultAuthenticationManager(null, username, password, new File(privateKeyFile), passphrase, getStoreFlag());
        initialiseRepository(url);
    }

    private boolean getStoreFlag()
    {
        DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(null, true);
        return options.isAuthStorageEnabled();
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

    protected void finalize() throws Throwable
    {
        super.finalize();
        close();
    }

    //=======================================================================
    // ScmClient interface
    //=======================================================================

    public void init(ScmContext context, ScmFeedbackHandler handler) throws ScmException
    {
        // noop
    }

    public void destroy(ScmContext context, ScmFeedbackHandler handler) throws ScmException
    {
        // noop
    }

    public void close()
    {
        if (repository != null)
        {
            try
            {
                repository.closeSession();
            }
            catch (Exception e)
            {
                LOG.warning(e);
            }

            repository = null;
        }
    }

    public Set<ScmCapability> getCapabilities(ScmContext context)
    {
        return EnumSet.allOf(ScmCapability.class);
    }

    public Map<String, String> getServerInfo() throws ScmException
    {
        // Unfortunately we can't find out much about the server, we just
        // know where it is
        Map<String, String> info = new TreeMap<String, String>();
        info.put("location", repository.getLocation().toString());
        return info;
    }

    public String getUid() throws ScmException
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

    public List<ResourceProperty> getProperties(ExecutionContext context) throws ScmException
    {
        return Arrays.asList(new ResourceProperty("svn.url", url));
    }

    public void testConnection() throws ScmException
    {
        try
        {
            repository.testConnection();
            SVNNodeKind kind = repository.checkPath(".", SVNRevision.HEAD.getNumber());
            if(kind == SVNNodeKind.NONE)
            {
                throw new ScmException("Path '" + repository.getLocation().getPath() + "' does not exist in the repository");
            }
        }
        catch (SVNException e)
        {
            throw new ScmException(e);
        }
    }

    public Revision checkout(ExecutionContext context, Revision revision, ScmFeedbackHandler handler) throws ScmException
    {
        SVNRevision svnRevision;
        SVNUpdateClient updateClient = new SVNUpdateClient(repository.getAuthenticationManager(), null);

        if (revision == null)
        {
            svnRevision = convertRevision(getLatestRevision(null));
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
            updateClient.doCheckout(repository.getLocation(), context.getWorkingDir(), SVNRevision.UNDEFINED, svnRevision, SVNDepth.INFINITY, false);
            updateExternals(context.getWorkingDir(), revision, updateClient, handler);
        }
        catch (SVNException e)
        {
            throw convertException(e);
        }

        return new Revision(svnRevision.getNumber());
    }

    private void updateExternals(File toDirectory, Revision revision, SVNUpdateClient client, ScmFeedbackHandler handler) throws ScmException
    {
        List<ExternalDefinition> externals = getExternals(revision);
        for (ExternalDefinition external : externals)
        {
            if (handler != null)
            {
                handler.status("Processing external '" + external.path + "'");
            }

            update(new File(toDirectory, FileSystemUtils.localiseSeparators(external.path)), convertRevision(revision), client);

            if (handler != null)
            {
                handler.status("External updated to revision " + revision.getRevisionString());
            }
        }
    }

    public InputStream retrieve(ScmContext context, String path, Revision revision) throws ScmException
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024);

        try
        {
            repository.getFile(path, convertRevision(revision).getNumber(), null, os);
        }
        catch (SVNException e)
        {
            throw convertException(e);
        }

        return new ByteArrayInputStream(os.toByteArray());
    }

    private boolean reportChanges(ChangeHandler handler, Revision from, Revision to) throws ScmException
    {
        if (to == null)
        {
            to = getLatestRevision(null);
        }

        long fromNumber = convertRevision(from).getNumber() + 1;
        long toNumber = convertRevision(to).getNumber();

        if (fromNumber <= toNumber)
        {
            try
            {
                if (log(repository, fromNumber, toNumber, handler))
                {
                    return true;
                }

                List<ExternalDefinition> externals = getExternals(to);
                for (ExternalDefinition external : externals)
                {
                    SVNRepository repo = SVNRepositoryFactory.create(external.url);
                    repo.setAuthenticationManager(authenticationManager);
                    if (log(repo, fromNumber, toNumber, handler))
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

    private boolean log(SVNRepository repository, long fromNumber, long toNumber, ChangeHandler handler) throws SVNException, ScmException
    {
        List<SVNLogEntry> logs = new LinkedList<SVNLogEntry>();
        PathFilter filter = new ExcludePathFilter(excludedPaths);

        repository.log(new String[]{""}, logs, fromNumber, toNumber, true, true);
        for (SVNLogEntry entry : logs)
        {
            Revision revision = new Revision(entry.getRevision());
            handler.startChangelist(revision, entry.getDate().getTime(), entry.getAuthor(), entry.getMessage());

            Map files = entry.getChangedPaths();

            for (Object value : files.values())
            {
                SVNLogEntryPath entryPath = (SVNLogEntryPath) value;
                if (filter.accept(entryPath.getPath()))
                {
                    if (handler.handleChange(new FileChange(entryPath.getPath(), revision, decodeAction(entryPath.getType()))))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    List<ExternalDefinition> getExternals(Revision revision) throws ScmException
    {
        List<ExternalDefinition> result = new LinkedList<ExternalDefinition>();
        if (externalsPaths.size() > 0)
        {
            try
            {
                SVNWCClient wcClient = new SVNWCClient(repository.getAuthenticationManager(), null);
                for (String externalsPath : externalsPaths)
                {
                    SVNURL url = repository.getLocation().appendPath(externalsPath, false);
                    SVNPropertyData data = wcClient.doGetProperty(url, SVNProperty.EXTERNALS, SVNRevision.UNDEFINED, convertRevision(revision));
                    if (data == null)
                    {
                        LOG.warning("Configured externals path '" + externalsPath + "' for URL '" + repository.getLocation().toString() + "' does not exist or does not have svn:externals property set: ignoring.");
                    }
                    else
                    {
                        addExternalsFromProperty(StringUtils.join("/", true, true, externalsPath, data.getValue().getString()), result);
                    }
                }
            }
            catch (IOException e)
            {
                throw new ScmException("I/O error checking externals: " + e.getMessage(), e);
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

    public List<Changelist> getChanges(ScmContext context, Revision from, Revision to) throws ScmException
    {
        ChangelistAccumulator accumulator = new ChangelistAccumulator();
        reportChanges(accumulator, from, to);
        return accumulator.getChangelists();
    }

    public List<Revision> getRevisions(ScmContext context, Revision from, Revision to) throws ScmException
    {
        List<Changelist> changes = getChanges(null, from, to);
        Collections.sort(changes);

        List<Revision> result = new LinkedList<Revision>();
        for (Changelist change : changes)
        {
            result.add(change.getRevision());
        }

        return result;
    }

    public boolean hasChangedSince(Revision since) throws ScmException
    {
        Revision latestRevision = getLatestRevision(null);
        if (latestRevision != since)
        {
            ChangeDetector detector = new ChangeDetector();
            reportChanges(detector, since, latestRevision);
            return detector.isChanged();
        }
        else
        {
            return false;
        }
    }

    public Revision getLatestRevision(ScmContext context) throws ScmException
    {
        try
        {
            long revision = getLatestRepositoryRevision(repository);
            
            for (ExternalDefinition external: getExternals(null))
            {
                SVNRepository repo = SVNRepositoryFactory.create(external.url);
                repo.setAuthenticationManager(authenticationManager);
                long externalRevision = getLatestRepositoryRevision(repo);
                if (externalRevision > revision)
                {
                    revision = externalRevision;
                }
            }

            return new Revision(revision);
        }
        catch (SVNException e)
        {
            throw convertException(e);
        }
    }

    private long getLatestRepositoryRevision(SVNRepository repository) throws SVNException, ScmException
    {
        SVNDirEntry entry = repository.info(".", SVNRevision.HEAD.getNumber());
        if (entry == null)
        {
            throw new ScmException("Unable to retrieve latest revision: no repository found at '" + repository.getLocation() + "'");
        }

        return entry.getRevision();
    }

    public List<ScmFile> browse(ScmContext context, String path, Revision revision) throws ScmException
    {
        LinkedList<SVNDirEntry> files = new LinkedList<SVNDirEntry>();
        try
        {
            repository.getDir(path, -1, null, files);
        }
        catch (SVNException e)
        {
            if (e.getMessage().endsWith("Can't get entries of non-directory"))
            {
                return Arrays.asList(new ScmFile(path));
            }
            throw convertException(e);
        }

        List<ScmFile> result = new LinkedList<ScmFile>();

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

            ScmFile f = new ScmFile(path, e.getName(), isDir);
            result.add(f);
        }

        return result;
    }

    public Revision update(ExecutionContext context, Revision rev, ScmFeedbackHandler handler) throws ScmException
    {
        // CIB-610: cleanup before update in case WC is locked.
        SVNWCClient wcClient = new SVNWCClient(authenticationManager, null);
        try
        {
            wcClient.doCleanup(context.getWorkingDir());
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

        update(context.getWorkingDir(), convertRevision(rev), client);
        updateExternals(context.getWorkingDir(), rev, client, handler);
        return rev;
    }

    private void update(File workDir, SVNRevision rev, SVNUpdateClient client) throws ScmException
    {
        try
        {
            client.doUpdate(workDir, rev, SVNDepth.INFINITY, false, false);
        }
        catch (SVNException e)
        {
            throw convertException(e);
        }
    }

    boolean pathExists(SVNURL path) throws SVNException
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

    public void tag(ScmContext scmContent, ExecutionContext context, Revision revision, String name, boolean moveExisting) throws ScmException
    {
        try
        {
            SVNURL svnUrl = SVNURL.parseURIDecoded(name);

            if (pathExists(svnUrl))
            {
                if (moveExisting)
                {
                    // Delete existing path
                    SVNCommitClient commitClient = new SVNCommitClient(authenticationManager, null);
                    commitClient.doDelete(new SVNURL[] { svnUrl }, "[pulse] deleting old tag");
                }
                else
                {
                    throw new ScmException("Unable to apply tag: path '" + name + "' already exists in the repository");
                }
            }

            SVNCopyClient copyClient = new SVNCopyClient(authenticationManager, null);
            SVNRevision copyRevision = convertRevision(revision);
            SVNCopySource[] copySources = new SVNCopySource[]{new SVNCopySource(SVNRevision.UNDEFINED, copyRevision, repository.getLocation())};
            copyClient.doCopy(copySources, svnUrl, false, true, true, "[pulse] applying tag", null);
        }
        catch (SVNException e)
        {
            throw convertException(e);
        }
    }

    public void storeConnectionDetails(ExecutionContext context, File outputDir) throws ScmException, IOException
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

    public EOLStyle getEOLPolicy(ScmContext context)
    {
        return EOLStyle.BINARY;
    }

    public Revision parseRevision(ScmContext context, String revision) throws ScmException
    {
        try
        {
            long revisionNumber = Long.parseLong(revision);
            if(revisionNumber > repository.getLatestRevision())
            {
                throw new ScmException("Revision '" + revision + "' does not exist in this repository");
            }

            return new Revision(revisionNumber);
        }
        catch(NumberFormatException e)
        {
            throw new ScmException("Invalid revision '" + revision + "': must be a valid revision number");
        }
        catch (SVNException e)
        {
            throw convertException(e);
        }
    }

    public Revision getPreviousRevision(ScmContext context, Revision revision, boolean isFile) throws ScmException
    {
        try
        {
            return revision.calculatePreviousNumericalRevision();
        }
        catch (NumberFormatException e)
        {
            throw new ScmException("Invalid revision '" + revision.getRevisionString() + "': " + e.getMessage());
        }
    }

    public List<Feature> applyPatch(ExecutionContext context, File patchFile, File baseDir, EOLStyle localEOL, ScmFeedbackHandler scmFeedbackHandler) throws ScmException
    {
        return StandardPatchFileSupport.applyPatch(patchFile, baseDir, localEOL, scmFeedbackHandler);
    }

    public List<FileStatus> readFileStatuses(ScmContext context, File patchFile) throws ScmException
    {
        return StandardPatchFileSupport.readFileStatuses(patchFile);
    }
    
    private static class ChangeEventHandler implements ISVNEventHandler
    {
        private static final String LABEL_NONE       = " ";
        private static final String LABEL_ADD        = "A";
        private static final String LABEL_DELETE     = "D";
        private static final String LABEL_CHANGED    = "U";
        private static final String LABEL_CONFLICTED = "C";
        private static final String LABEL_MERGED     = "G";
        private static final String LABEL_LOCKED     = "L";
        private static final String LABEL_UNLOCKED   = "B";

        private static final String FORMAT_LABELS = "%s%s%s  ";

        private ScmFeedbackHandler handler;

        public ChangeEventHandler(ScmFeedbackHandler handler)
        {
            this.handler = handler;
        }

        public void handleEvent(SVNEvent event, double progress)
        {
            SVNEventAction action = event.getAction();
            if (action != null)
            {
                if (action == SVNEventAction.UPDATE_EXTERNAL)
                {
                    handler.status("Fetching external item into '" + event.getFile().getAbsolutePath() + "'");
                    if (event.getRevision() > 0)
                    {
                        handler.status("External at revision " + event.getRevision());
                    }
                }
                else
                {
                    String pathChangeLabel;
                    String propertiesChangeLabel = LABEL_NONE;
                    String lockLabel = LABEL_NONE;

                    if (action == SVNEventAction.ADD)
                    {
                        pathChangeLabel = LABEL_ADD;
                    }
                    else if (action == SVNEventAction.DELETE)
                    {
                        pathChangeLabel = LABEL_DELETE;
                    }
                    else if (action == SVNEventAction.LOCKED)
                    {
                        pathChangeLabel = LABEL_LOCKED;
                    }
                    else if (action == SVNEventAction.LOCK_FAILED)
                    {
                        // Does not fit formatting, but this should not happen and
                        // if it does then obvious output is good anyway.
                        pathChangeLabel = "Failed to lock";
                    }
                    else
                    {
                        pathChangeLabel = getPathChangeType(action, event.getContentsStatus());
                        propertiesChangeLabel = convertStatusType(event.getPropertiesStatus());
                        lockLabel = convertStatusType(event.getLockStatus());
                    }

                    String labels = String.format(FORMAT_LABELS, pathChangeLabel, propertiesChangeLabel, lockLabel);
                    if (TextUtils.stringSet(labels.trim()))
                    {
                        handler.status(labels + event.getFile().getPath());
                    }
                }
            }
        }

        private String getPathChangeType(SVNEventAction action, SVNStatusType contentsStatus)
        {
            if (action == SVNEventAction.UPDATE_ADD)
            {
                return LABEL_ADD;
            }
            else if (action == SVNEventAction.UPDATE_DELETE)
            {
                return LABEL_DELETE;
            }
            else if (action == SVNEventAction.UPDATE_UPDATE)
            {
                return convertStatusType(contentsStatus);
            }
            else
            {
                return LABEL_NONE;
            }
        }

        private static String convertStatusType(SVNStatusType status)
        {
            if (status == SVNStatusType.CHANGED)
            {
                return LABEL_CHANGED;
            }
            else if (status == SVNStatusType.CONFLICTED)
            {
                return LABEL_CONFLICTED;
            }
            else if (status == SVNStatusType.MERGED)
            {
                return LABEL_MERGED;
            }
            else if (status == SVNStatusType.LOCK_UNLOCKED)
            {
                return LABEL_UNLOCKED;
            }
            else
            {
                return LABEL_NONE;
            }
        }

        public void checkCancelled() throws SVNCancelException
        {
            try
            {
                handler.checkCancelled();
            }
            catch (ScmCancelledException e)
            {
                throw new SVNCancelException();
            }
        }
    }

    private interface ChangeHandler
    {
        void startChangelist(Revision revision, long time, String author, String message);

        boolean handleChange(FileChange change);

        void complete();
    }

    private static class ChangelistAccumulator implements ChangeHandler
    {
        private List<Changelist> changelists = new LinkedList<Changelist>();
        private List<FileChange> currentChanges = null;
        private Revision currentRevision;
        private long currentTime;
        private String currentAuthor;
        private String currentMessage;

        public List<Changelist> getChangelists()
        {
            return changelists;
        }

        public void startChangelist(Revision revision, long time, String author, String message)
        {
            checkCurrent();
            currentChanges = new LinkedList<FileChange>();
            currentRevision = revision;
            currentTime = time;
            currentAuthor = author;
            currentMessage = message;
        }

        public boolean handleChange(FileChange change)
        {
            currentChanges.add(change);
            return false;
        }

        public void complete()
        {
            checkCurrent();
        }

        private void checkCurrent()
        {
            if (currentChanges != null && !currentChanges.isEmpty())
            {
                for (Changelist list : changelists)
                {
                    if (list.getRevision().getRevisionString().equals(currentRevision.getRevisionString()))
                    {
                        // We have already seen this log entry in another external
                        return;
                    }
                }

                changelists.add(new Changelist(currentRevision, currentTime, currentAuthor, currentMessage, currentChanges));
            }
        }
    }

    private static class ChangeDetector implements ChangeHandler
    {
        private boolean changed = false;

        public boolean isChanged()
        {
            return changed;
        }

        public void startChangelist(Revision revision, long time, String author, String message)
        {
        }

        public boolean handleChange(FileChange change)
        {
            changed = true;
            return true;
        }

        public void complete()
        {
        }
    }

    static class ExternalDefinition
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
