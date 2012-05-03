package com.zutubi.pulse.core.scm.svn;

import com.zutubi.i18n.Messages;
import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.engine.api.ResourceProperty;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.util.ConjunctivePredicate;
import com.zutubi.util.Predicate;
import com.zutubi.util.StringUtils;
import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.dav.http.DefaultHTTPConnectionFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.internal.wc.SVNExternal;
import org.tmatesoft.svn.core.internal.wc.admin.SVNAdminAreaFactory;
import org.tmatesoft.svn.core.io.*;
import org.tmatesoft.svn.core.io.diff.SVNDiffWindow;
import org.tmatesoft.svn.core.wc.*;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;

import java.io.*;
import java.util.*;

/**
 * A connection to a subversion server.
 */
public class SubversionClient implements ScmClient
{
    public static final String TYPE = "svn";

    private static final Logger LOG = Logger.getLogger(ScmClient.class);
    private static final Messages I18N = Messages.getInstance(SubversionClient.class);
    
    private static final String PROPERTY_HTTP_AUTH_METHODS = "svnkit.http.methods";
    private static final String HTTP_AUTH_METHODS = "Digest,Basic,Negotiate,NTLM";

    private boolean cleanOnUpdateFailure = true;
    private boolean useExport = false;
    /**
     * If true, we will monitor all externals by recursively scanning for
     * svn:externals properties.  We also recurse to find externals defined
     * within externals.
     */
    private boolean monitorAllExternals = false;
    /**
     * A list of paths within the main repository for which we will check the
     * svn:externals property.  If any externals are set to paths within the
     * *same* repository, they will be taken into account in checkout, update
     * and change checking operations.
     * <p/>
     * Ignored when {@link #monitorAllExternals} is true.
     */
    private List<String> externalsPaths = new LinkedList<String>();
    private boolean verifyExternals = true;
    private List<String> includedPaths = new LinkedList<String>();
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
        if (e instanceof SVNCancelException)
        {
            return new ScmCancelledException(e.getMessage(), e);
        }
        else
        {
            return new ScmException(e.getMessage(), e);
        }
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
     * @param url                the URL to connect to the server on
     * @param enableHttpSpooling set to true if we should enable spooling (to
     *                           a temp file) for HTTP downloads in SvnKit
     * @throws ScmException if an error occurs connecting to the server
     */
    private void initialiseRepository(String url, boolean enableHttpSpooling) throws ScmException
    {
        // Workaround for CIB-1978, SvnKit bug 238:
        // http://svnkit.com/tracker/bug_view_advanced_page.php?bug_id=283
        if (System.getProperty(PROPERTY_HTTP_AUTH_METHODS) == null)
        {
            System.setProperty(PROPERTY_HTTP_AUTH_METHODS, HTTP_AUTH_METHODS);
        }

        // Initialise SVN library
        if (enableHttpSpooling)
        {
            DAVRepositoryFactory.setup(new DefaultHTTPConnectionFactory(null, true, null));   
        }
        else
        {
            DAVRepositoryFactory.setup();
        }

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
     * @param url                the url of the SVN repository
     * @param enableHttpSpooling if true, use spooling for HTTP connections
     * @throws com.zutubi.pulse.core.scm.api.ScmException on error
     */
    public SubversionClient(String url, boolean enableHttpSpooling) throws ScmException
    {
        authenticationManager = SVNWCUtil.createDefaultAuthenticationManager();
        initialiseRepository(url, enableHttpSpooling);
    }

    /**
     * Creates a new SVNServer using the given location and credentials to
     * connect to the server.
     *
     * @param url                url location of the server and module to use
     * @param enableHttpSpooling if true, use spooling for HTTP connections
     * @param username           username to provide on connection
     * @param password           password for the given user
     * @throws ScmException if a connection cannot be established
     */
    public SubversionClient(String url, boolean enableHttpSpooling, String username, String password) throws ScmException
    {
        authenticationManager = SVNWCUtil.createDefaultAuthenticationManager(username, password);
        initialiseRepository(url, enableHttpSpooling);
    }

    /**
     * Creates a new SVNServer using the given location and credentials to
     * connect to the server.
     *
     * @param url                url location of the server and module to use
     * @param enableHttpSpooling if true, use spooling for HTTP connections
     * @param username           username to provide on connection
     * @param password           password for the given user
     * @param privateKeyFile     location of the private key to provide on login
     * @throws ScmException if a connection cannot be established
     */
    public SubversionClient(String url, boolean enableHttpSpooling, final String username, final String password, final String privateKeyFile) throws ScmException
    {
        authenticationManager = SVNWCUtil.createDefaultAuthenticationManager(null, username, password, new File(privateKeyFile), null, getStoreFlag());
        initialiseRepository(url, enableHttpSpooling);
    }

    /**
     * Creates a new SVNServer using the given location and credentials to
     * connect to the server.
     *
     * @param url                url location of the server and module to use
     * @param enableHttpSpooling if true, use spooling for HTTP connections
     * @param username           username to provide on connection
     * @param password           password for the given user
     * @param privateKeyFile     location of the private key to provide on login
     * @param passphrase         passphrase for the given private key file
     * @throws ScmException if a connection cannot be established
     */
    public SubversionClient(String url, boolean enableHttpSpooling, final String username, final String password, final String privateKeyFile, final String passphrase) throws ScmException
    {
        authenticationManager = SVNWCUtil.createDefaultAuthenticationManager(null, username, password, new File(privateKeyFile), passphrase, getStoreFlag());
        initialiseRepository(url, enableHttpSpooling);
    }

    private boolean getStoreFlag()
    {
        DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(null, true);
        return options.isAuthStorageEnabled();
    }

    public void setFilterPaths(List<String> includedPaths, List<String> excludedPaths)
    {
        this.includedPaths = includedPaths;
        this.excludedPaths = excludedPaths;
    }

    public void setCleanOnUpdateFailure(boolean cleanOnUpdateFailure)
    {
        this.cleanOnUpdateFailure = cleanOnUpdateFailure;
    }

    public void setUseExport(boolean useExport)
    {
        this.useExport = useExport;
    }

    public void setMonitorAllExternals(boolean monitorAllExternals)
    {
        this.monitorAllExternals = monitorAllExternals;
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

    public String getImplicitResource()
    {
        return "subversion";
    }

    public void init(ScmContext context, ScmFeedbackHandler handler) throws ScmException
    {
        // noop
    }

    public void destroy(ScmContext context, ScmFeedbackHandler handler) throws ScmException
    {
        // noop
    }

    private void close(SVNRepository repository)
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
        }
    }

    public void close()
    {
        close(repository);
        repository = null;
    }

    public Set<ScmCapability> getCapabilities(ScmContext context)
    {
        return EnumSet.complementOf(EnumSet.of(ScmCapability.EMAIL));
    }

    public Map<String, String> getServerInfo() throws ScmException
    {
        // Unfortunately we can't find out much about the server, we just
        // know where it is
        Map<String, String> info = new TreeMap<String, String>();
        info.put("location", repository.getLocation().toString());
        return info;
    }

    public String getUid(ScmContext context) throws ScmException
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

    public String getLocation(ScmContext context)
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
            SVNNodeKind kind = repository.checkPath("", SVNRevision.HEAD.getNumber());
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
            if (!context.getBoolean(NAMESPACE_INTERNAL, PROPERTY_INCREMENTAL_BOOTSTRAP, false) && useExport)
            {
                updateClient.doExport(repository.getLocation(), context.getWorkingDir(), SVNRevision.UNDEFINED, svnRevision, null, true, SVNDepth.INFINITY);
            }
            else
            {
                if (useExport)
                {
                    addIgnoreDirs(context);
                }
                
                updateClient.doCheckout(repository.getLocation(), context.getWorkingDir(), SVNRevision.UNDEFINED, svnRevision, SVNDepth.INFINITY, false);
                updateExternals(context.getWorkingDir(), revision, updateClient, handler);
            }
        }
        catch (SVNException e)
        {
            throw convertException(e);
        }
        finally
        {
            dispose(updateClient);
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

        SVNRevision fromRevision = convertRevision(from);
        SVNRevision toRevision = convertRevision(to);

        if (fromRevision.getNumber() != toRevision.getNumber())
        {
            long fromNumber;
            long toNumber;
            if (fromRevision.getNumber() < toRevision.getNumber())
            {
                fromNumber = fromRevision.getNumber() + 1;
                toNumber = toRevision.getNumber();
            }
            else
            {
                fromNumber = fromRevision.getNumber();
                toNumber = toRevision.getNumber() + 1;
            }
            try
            {
                if (log(repository, fromNumber, toNumber, handler))
                {
                    return true;
                }

                List<ExternalDefinition> externals = getExternals(to);
                for (ExternalDefinition external : externals)
                {
                    SVNRepository repo = null;
                    try
                    {
                        repo = SVNRepositoryFactory.create(external.url);
                        repo.setAuthenticationManager(authenticationManager);
                        if (log(repo, fromNumber, toNumber, handler))
                        {
                            return true;
                        }
                    }
                    finally
                    {
                        close(repo);
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
        Predicate<String> changelistFilter = new FilterPathsPredicate(includedPaths, excludedPaths);

        repository.log(new String[]{""}, logs, fromNumber, toNumber, true, true);

        if (containsUnfilteredChanges(logs, changelistFilter, repository))
        {
            // If we have changes, remove only those paths that are explicitly filtered.
            for (SVNLogEntry entry : logs)
            {
                Revision revision = new Revision(entry.getRevision());
                handler.startChangelist(revision, entry.getDate().getTime(), entry.getAuthor(), entry.getMessage());

                Map files = entry.getChangedPaths();

                for (Object value : files.values())
                {
                    SVNLogEntryPath entryPath = (SVNLogEntryPath) value;
                    if (changelistFilter.satisfied(entryPath.getPath()))
                    {
                        if (handler.handleChange(new FileChange(entryPath.getPath(), revision, decodeAction(entryPath.getType()))))
                        {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * This method returns true if there are changes within the logs that are
     * <ul>
     * <li>within the specified repository path</li>
     * <li>not filtered out by the change filter</li>
     * </ul>
     *
     * @param logs          the changes being checked
     * @param changeFilter  the predicate that is only satisfied by unfiltered paths
     * @param repository    the repository defines the repository path used in the
     * filtering.
     * 
     * @return true if there are changes within the logs that are not filtered.
     *
     * @throws SVNException if there is a problem accessing the repository.
     */
    private boolean containsUnfilteredChanges(List<SVNLogEntry> logs, Predicate<String> changeFilter, SVNRepository repository) throws SVNException
    {
        Predicate<String> hasChangedFilter = new ConjunctivePredicate<String>(
                changeFilter,
                new PrefixPathFilter(repository.getRepositoryPath(""))
        );

        for (SVNLogEntry entry : logs)
        {
            Map files = entry.getChangedPaths();
            for (Object value : files.values())
            {
                SVNLogEntryPath entryPath = (SVNLogEntryPath) value;
                if (hasChangedFilter.satisfied(entryPath.getPath()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    List<ExternalDefinition> getExternals(Revision revision) throws ScmException
    {
        final List<ExternalDefinition> result = new LinkedList<ExternalDefinition>();
        try
        {
            long rev = revision == null ? repository.getLatestRevision() : convertRevision(revision).getNumber();
            if (monitorAllExternals)
            {
                addExternalsFromUrl(rev, "", repository.getLocation(), SVNDepth.INFINITY, result);
            }
            else if (externalsPaths.size() > 0)
            {
                for (String externalsPath: externalsPaths)
                {
                    if (externalsPath.equals("."))
                    {
                        externalsPath = "";
                    }
                    
                    SVNURL url = repository.getLocation().appendPath(externalsPath, false);
                    addExternalsFromUrl(rev, externalsPath, url, SVNDepth.EMPTY, result);
                }
            }
        }
        catch (SVNException e)
        {
            throw convertException(e);
        }

        return result;
    }

    private void addExternalsFromUrl(final long revision, final String ownerPath, final SVNURL ownerURL, final SVNDepth depth, final List<ExternalDefinition> result) throws SVNException
    {
        SVNRepository repository = null;
        try
        {
            repository = SVNRepositoryFactory.create(ownerURL);
            repository.setAuthenticationManager(authenticationManager);

            ISVNReporterBaton reporter = new ISVNReporterBaton()
            {
                public void report(ISVNReporter reporter) throws SVNException
                {
                    reporter.setPath("", null, revision, depth, true);
                    reporter.finishReport();
                }
            };

            repository.status(revision, null, depth, reporter, new GetExternalsEditor(revision, ownerPath, ownerURL, result));
        }
        finally
        {
            close(repository);
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
                SVNRepository repo = null;
                try
                {
                    repo = SVNRepositoryFactory.create(external.url);
                    repo.setAuthenticationManager(authenticationManager);
                    long externalRevision = getLatestRepositoryRevision(repo);
                    if (externalRevision > revision)
                    {
                        revision = externalRevision;
                    }
                }
                finally
                {
                    close(repo);
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
        SVNDirEntry entry = repository.info("", SVNRevision.HEAD.getNumber());
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
        if (rev == null)
        {
            rev = getLatestRevision(null);
        }

        if (useExport)
        {
            addIgnoreDirs(context);
        }

        SVNUpdateClient client = null;
        try
        {
            // CIB-610: cleanup before update in case WC is locked.
            cleanup(context);

            client = new SVNUpdateClient(authenticationManager, null);
            if (handler != null)
            {
                client.setEventHandler(new ChangeEventHandler(handler));
            }

            update(context.getWorkingDir(), convertRevision(rev), client);
            updateExternals(context.getWorkingDir(), rev, client, handler);
        }
        catch (ScmCancelledException e)
        {
            throw new ScmCancelledException(e);
        }
        catch (ScmException e)
        {
            rev = handleUpdateError(context, rev, handler, e.getMessage());
            if (rev == null)
            {
                throw new ScmException(e);
            }
        }
        finally
        {
            dispose(client);
        }
        return rev;
    }

    private void addIgnoreDirs(ExecutionContext context)
    {
        context.addString(NAMESPACE_INTERNAL, PROPERTY_IGNORE_DIRS, ".svn");
    }

    private void cleanup(ExecutionContext context) throws ScmException
    {
        SVNWCClient wcClient = null;
        try
        {
            wcClient = new SVNWCClient(authenticationManager, null);
            wcClient.doCleanup(context.getWorkingDir());
        }
        catch (SVNException e)
        {
            throw convertException(e);
        }
        finally
        {
            dispose(wcClient);
        }
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

    /**
     * Checks for a known class of unrecoverable errors on update, and
     * automatically tries a clean checkout in this case (to save the user the
     * effort of doing this manually).
     * 
     * @param context      context in which the update is running
     * @param rev          revision to update to
     * @param handler      used to capture feedback from the update/checkout
     * @param errorMessage error message returned from the update attempt
     * @return the revision checked out, if an automatic fix was attempted, or
     *         null if the original error should just propagate
     * @throws ScmException on error cleaning up or checking out
     */
    private Revision handleUpdateError(ExecutionContext context, Revision rev, ScmFeedbackHandler handler, String errorMessage) throws ScmException
    {
        if (cleanOnUpdateFailure)
        {
            File wcDir = context.getWorkingDir();
            if (handler != null)
            {
                handler.status(I18N.format("update.error", errorMessage));
                handler.status(I18N.format("update.error.attempting.clean"));
            }

            try
            {
                FileSystemUtils.rmdir(wcDir);
            }
            catch (IOException e)
            {
                throw new ScmException(I18N.format("update.error.cannot.remove.dir", e.getMessage()), e);
            }

            if (!wcDir.mkdirs())
            {
                throw new ScmException(I18N.format("update.error.cannot.create.dir", wcDir.getAbsolutePath()));
            }

            return checkout(context, rev, handler);
        }
        
        return null;
    }

    boolean pathExists(SVNURL path) throws SVNException
    {
        SVNRepository repo = null;
        try
        {
            repo = SVNRepositoryFactory.create(path);
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
        finally
        {
            close(repo);
        }
    }

    public void tag(ScmContext scmContent, Revision revision, String name, boolean moveExisting) throws ScmException
    {
        SVNCommitClient commitClient = null;
        SVNCopyClient copyClient = null;
        try
        {
            SVNURL svnUrl = SVNURL.parseURIDecoded(name);

            if (pathExists(svnUrl))
            {
                if (moveExisting)
                {
                    // Delete existing path
                    commitClient = new SVNCommitClient(authenticationManager, null);
                    commitClient.doDelete(new SVNURL[] { svnUrl }, "[pulse] deleting old tag");
                }
                else
                {
                    throw new ScmException("Unable to apply tag: path '" + name + "' already exists in the repository");
                }
            }

            copyClient = new SVNCopyClient(authenticationManager, null);
            SVNRevision copyRevision = convertRevision(revision);
            SVNCopySource[] copySources = new SVNCopySource[]{new SVNCopySource(SVNRevision.UNDEFINED, copyRevision, repository.getLocation())};
            copyClient.doCopy(copySources, svnUrl, false, true, true, "[pulse] applying tag", null);
        }
        catch (SVNException e)
        {
            throw convertException(e);
        }
        finally
        {
            dispose(commitClient);
            dispose(copyClient);
        }
    }

    private void dispose(SVNBasicClient client)
    {
        // This should really be handled by SVNKit, but alas it leaks like a sieve.
        if (client != null)
        {
            SvnOperationFactory operationsFactory = client.getOperationsFactory();
            if (operationsFactory != null)
            {
                ISVNRepositoryPool repositoryPool = operationsFactory.getRepositoryPool();
                if (repositoryPool != null)
                {
                    repositoryPool.dispose();
                }

                operationsFactory.dispose();
            }
        }
    }

    public void storeConnectionDetails(ExecutionContext context, File outputDir) throws ScmException, IOException
    {
        Properties props = new Properties();
        props.put("location", getLocation(null));

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

    public EOLStyle getEOLPolicy(ExecutionContext context)
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

    public String getEmailAddress(ScmContext context, String user) throws ScmException
    {
        throw new ScmException("Operation not supported");
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
                    if (StringUtils.stringSet(labels.trim()))
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

        public ExternalDefinition(String path, SVNURL url)
        {
            this.path = path;
            this.url = url;
        }

        public ExternalDefinition(String path, String url) throws SVNException
        {
            this(path, SVNURL.parseURIDecoded(url));
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            ExternalDefinition that = (ExternalDefinition) o;

            return path.equals(that.path) && url.equals(that.url);

        }

        @Override
        public int hashCode()
        {
            int result = path.hashCode();
            result = 31 * result + url.hashCode();
            return result;
        }

        @Override
        public String toString()
        {
            return path + " -> "+ url;
        }
    }

    /**
     * Editor implementation to gather svn:externals values from directory as
     * part of externals collection.
     */
    private class GetExternalsEditor implements ISVNEditor
    {
        private Stack<String> dirStack;
        private final long revision;
        private final String ownerPath;
        private final SVNURL ownerURL;
        private final List<ExternalDefinition> result;

        public GetExternalsEditor(long revision, String ownerPath, SVNURL ownerURL, List<ExternalDefinition> result)
        {
            this.revision = revision;
            this.ownerPath = ownerPath;
            this.ownerURL = ownerURL;
            this.result = result;
            dirStack = new Stack<String>();
        }

        public void targetRevision(long revision) throws SVNException
        {
        }

        public void openRoot(long revision) throws SVNException
        {
            dirStack.push("");
        }

        public void deleteEntry(String path, long revision) throws SVNException
        {
        }

        public void absentDir(String path) throws SVNException
        {
        }

        public void absentFile(String path) throws SVNException
        {
        }

        public void addDir(String path, String copyFromPath, long copyFromRevision) throws SVNException
        {
            dirStack.push(path);
        }

        public void openDir(String path, long revision) throws SVNException
        {
            dirStack.push(path);
        }

        public void changeDirProperty(String name, SVNPropertyValue value) throws SVNException
        {
            if (SVNProperty.EXTERNALS.equals(name))
            {
                String dirPath = StringUtils.join("/", true, true, ownerPath, dirStack.peek());
                addExternalsFromProperty(revision, dirPath, ownerURL.appendPath(dirStack.peek(), false), value.getString(), result);
            }
        }

        private void addExternalsFromProperty(long revision, String ownerPath, SVNURL ownerURL, String value, List<ExternalDefinition> externals) throws SVNException
        {
            SVNExternal[] svnExternals = SVNExternal.parseExternals(ownerURL.toString(), value);
            for (SVNExternal external: svnExternals)
            {
                // We don't monitor or report changes when the revision is fixed.
                if (!external.isRevisionExplicit())
                {
                    ExternalDefinition definition = new ExternalDefinition(StringUtils.join("/", true, true, ownerPath, external.getPath()), external.resolveURL(repository.getRepositoryRoot(true), ownerURL));
                    if (verifyExternals)
                    {
                        SVNRepository repo = SVNRepositoryFactory.create(definition.url);
                        repo.setAuthenticationManager(authenticationManager);

                        try
                        {
                            String uid = repo.getRepositoryUUID(true);
                            if (uid.equals(getUid(null)))
                            {
                                addExternal(revision, definition, externals);
                            }
                            else
                            {
                                LOG.warning("Ignoring external at URL '" + definition.url.toDecodedString() + "': UID does not match");
                            }
                        }
                        catch (Exception e)
                        {
                            LOG.warning("Ignoring external at URL '" + definition.url.toDecodedString() + "'", e);
                        }
                    }
                    else
                    {
                        addExternal(revision, definition, externals);
                    }
                }
            }
        }

        private void addExternal(long revision, ExternalDefinition definition, List<ExternalDefinition> externals) throws SVNException
        {
            externals.add(definition);
            if (monitorAllExternals)
            {
                addExternalsFromUrl(revision, definition.path, definition.url, SVNDepth.INFINITY, externals);
            }
        }
        
        public void closeDir() throws SVNException
        {
            dirStack.pop();
        }

        public void addFile(String path, String copyFromPath, long copyFromRevision) throws SVNException
        {
        }

        public void openFile(String path, long revision) throws SVNException
        {
        }

        public void changeFileProperty(String path, String name, SVNPropertyValue value) throws SVNException
        {
        }

        public void closeFile(String path, String textChecksum) throws SVNException
        {
        }

        public SVNCommitInfo closeEdit() throws SVNException
        {
            return null;
        }

        public void abortEdit() throws SVNException
        {
        }

        public void applyTextDelta(String path, String baseChecksum) throws SVNException
        {
        }

        public OutputStream textDeltaChunk(String path, SVNDiffWindow diffWindow) throws SVNException
        {
            return null;
        }

        public void textDeltaEnd(String path) throws SVNException
        {
        }
    }
}
