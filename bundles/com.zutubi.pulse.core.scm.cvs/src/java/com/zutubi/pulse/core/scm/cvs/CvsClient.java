package com.zutubi.pulse.core.scm.cvs;

import com.google.common.base.Function;
import com.google.common.io.Files;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.engine.api.ResourceProperty;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.cvs.client.CvsCore;
import com.zutubi.pulse.core.scm.cvs.client.LogInformationAnalyser;
import com.zutubi.pulse.core.scm.cvs.client.commands.RlsInfo;
import com.zutubi.util.StringUtils;
import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.command.log.LogInformation;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;

/**
 * The CvsClient provides all interactions with a cvs repository.
 */
public class CvsClient implements ScmClient
{
    private static final Logger LOG = Logger.getLogger(CvsClient.class);

    protected static final String PROPERTY_CVS_ROOT = "cvs.root";
    protected static final String PROPERTY_CVS_BRANCH = "cvs.branch";
    protected static final String PROPERTY_CVS_MODULE = "cvs.module";
    protected static final String PROPERTY_CVS_MODULE_COUNT = "cvs.module.count";
    protected static final String PREFIX_CVS_MODULE = "cvs.module.";

    private File tmpSpace;

    private CvsCore core;

    /**
     * A record of the original configuration module string, formatted as a comma separated
     * list of modules.
     */
    private String module;

    /**
     * The parsed version of the module string.
     */
    private String[] modules;
    private String branch;
    private String root;
    private String password;

    private List<String> includedPaths = new LinkedList<String>();
    private List<String> excludedPaths = new LinkedList<String>();

    public CvsClient(String root, String module, String password, String branch)
    {
        core = new CvsCore();
        core.setRoot(CVSRoot.parse(root));
        core.setPassword(password);

        this.module = module;

        this.modules = parseModuleString(module);
        this.branch = branch;
        this.root = root;
        this.password = password;
    }

    private String[] parseModuleString(String module)
    {
        StringTokenizer tokens = new StringTokenizer(module, ", ", false);
        String[] modules = new String[tokens.countTokens()];
        for (int i = 0; tokens.hasMoreTokens(); i++)
        {
            modules[i] = tokens.nextToken();
        }
        return modules;
    }

    public CvsClient(String root, String module, String password, String branch, List<String> includedPaths, List<String> excludedPaths, File tempDir)
    {
        this(root, module, password, branch);
        setFilterPaths(includedPaths, excludedPaths);
        setTemporarySpace(tempDir);
    }

    public void setFilterPaths(List<String> included, List<String> excluded)
    {
        this.includedPaths = included;
        this.excludedPaths = excluded;
    }

    public String getImplicitResource()
    {
        return "cvs";
    }

    public void init(ScmContext context, ScmFeedbackHandler handler) throws ScmException
    {
    }

    public void destroy(ScmContext context, ScmFeedbackHandler handler) throws ScmException
    {
        // noop
    }

    public void close()
    {
    }

    public Set<ScmCapability> getCapabilities(ScmContext context)
    {
        Set<ScmCapability> capabilities = EnumSet.complementOf(EnumSet.of(ScmCapability.EMAIL));
        try
        {
            String version = (context != null) ? getContextVersion(context) : core.version();

            if (!CvsServerCapabilities.supportsRemoteListing(version))
            {
                capabilities.remove(ScmCapability.BROWSE);
            }
        }
        catch (Exception e)
        {
            LOG.warning(e);
        }
        return capabilities;
    }

    private String getContextVersion(ScmContext context) throws ScmException
    {
        File versionFile = new File(context.getPersistentContext().getPersistentWorkingDir(), ".version");
        if (!versionFile.isFile())
        {
            try
            {
                String version = core.version();
                File parent = versionFile.getParentFile();
                if (!parent.exists() && !parent.mkdirs())
                {
                    throw new IOException("Failed to create directory: " + parent.getCanonicalPath());
                }
                if (!versionFile.createNewFile())
                {
                    throw new IOException("Failed to create new file: " + versionFile.getCanonicalPath());
                }
                Files.write(version, versionFile, Charset.defaultCharset());
                return version;
            }
            catch (IOException e)
            {
                throw new ScmException(e);
            }
        }
        else
        {
            try
            {
                return Files.toString(versionFile, Charset.defaultCharset());
            }
            catch (IOException e)
            {
                throw new ScmException(e);
            }
        }
    }

    /**
     * Returns the unique identifier for this scm server. For CVS servers, this is the cvs root.
     *
     * @see com.zutubi.pulse.core.scm.api.ScmClient#getUid(com.zutubi.pulse.core.scm.api.ScmContext)
     * @param context
     */
    public String getUid(ScmContext context)
    {
        return getRoot();
    }

    public String getLocation(ScmContext context)
    {
        return String.format("%s[%s]", getRoot(), getModule());
    }

    public List<ResourceProperty> getProperties(ExecutionContext context) throws ScmException
    {
        List<ResourceProperty> result = new LinkedList<ResourceProperty>();
        result.add(new ResourceProperty(PROPERTY_CVS_ROOT, root));

        if (branch != null)
        {
            result.add(new ResourceProperty(PROPERTY_CVS_BRANCH, branch));
        }
        result.add(new ResourceProperty(PROPERTY_CVS_MODULE, module));
        if (modules.length > 1)
        {
            result.add(new ResourceProperty(PROPERTY_CVS_MODULE_COUNT, String.valueOf(modules.length)));
            for (int i = 0; i < modules.length; i++)
            {
                result.add(new ResourceProperty(PREFIX_CVS_MODULE + (i + 1), modules[i]));
            }
        }

        return result;
    }

    /**
     * Get the configured cvs root property.
     *
     * @return cvs root.
     */
    public String getRoot()
    {
        return root;
    }

    /**
     * Get the configured cvs password property.
     *
     * @return connection password.
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Get the configured cvs module property.
     *
     * @return module
     */
    public String getModule()
    {
        return module;
    }

    /**
     * Get the branch to which this cvs server instance is bound.
     *
     * @return branch name.
     */
    public String getBranch()
    {
        return branch;
    }

    /**
     * Run some diagnostics on the cvs configuration.
     *
     * @throws ScmException if there is a problem with the server connection or configuration that
     *                      prevents us from querying the cvs repository.
     */
    public void testConnection() throws ScmException
    {
        // Check the connection to the cvs repository.  This covers the cvs root and authentication.
        core.testConnection();

        // Check that the module is valid.
        checkModuleIsValid();
    }

    private File[] getSubdirectoriesContainingCvsDirectories(File base)
    {
        return base.listFiles(new FileFilter()
        {
            public boolean accept(File child)
            {
                return child.isDirectory() && new File(child, "CVS").isDirectory();
            }
        });
    }

    public void tag(ScmContext scmContent, Revision revision, String name, boolean moveExisting) throws ScmException
    {
        assertRevisionArgValid(revision);
        for (String module : modules)
        {
            core.tag(module, convertRevision(revision), name, moveExisting);
        }
    }

    public void storeConnectionDetails(ExecutionContext context, File outputDir) throws ScmException, IOException
    {
        Properties props = new Properties();
        props.put("root", root);
        if (branch != null)
        {
            props.put("branch", branch);
        }
        props.put("module", module);

        FileOutputStream os = null;
        try
        {
            os = new FileOutputStream(new File(outputDir, "cvs.properties"));
            props.store(os, "CVS connection properties");
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
        CvsRevision cvsRevision = new CvsRevision(revision);
        if (cvsRevision.getBranch() == null)
        {
            // As this is a user-specified value, we set the right branch for
            // them when it is left unspecified.
            cvsRevision.setBranch(branch);
        }

        return convertRevision(cvsRevision);
    }

    public Revision getPreviousRevision(ScmContext context, Revision revision, boolean isFile) throws ScmException
    {
        // cvs revisions are file based.  If we receive a request for a revision other than for
        // a file, it is likely to be a tag or 'simulated' changelist revision.  These types of
        // revisions we can do nothing about.
        if (!isFile)
        {
            return null;
        }

        try
        {
            String revisionString = revision.getRevisionString();
            String[] parts = revisionString.split("\\.");

            // we expect the left of the parts array to be even.
            // ie:  1.1, 1.2.3.1, etc etc.

            long lastPart = Long.parseLong(parts[parts.length - 1]);
            if (lastPart > 1)
            {
                parts[parts.length - 1] = String.valueOf(lastPart - 1);
                return new Revision(StringUtils.join(".", parts));
            }
            else
            {
                if (parts.length == 2)
                {
                    return null;
                }
                String[] target = new String[parts.length - 2];
                System.arraycopy(parts, 0, target, 0, target.length);
                return new Revision(StringUtils.join(".", target));
            }
        }
        catch (NumberFormatException e)
        {
            // just in case.
            return null;
        }
    }

    public String getEmailAddress(ScmContext context, String user) throws ScmException
    {
        throw new ScmException("Operation not supported");
    }

    public Revision checkout(ExecutionContext context, Revision revision, ScmFeedbackHandler handler) throws ScmException
    {
        if (revision == Revision.HEAD)
        {
            revision = getLatestRevision(null);
        }

        for (String module : modules)
        {
            core.checkout(context.getWorkingDir(), module, convertRevision(revision), handler);
        }

        return revision;
    }

    public Revision update(ExecutionContext context, Revision rev, ScmFeedbackHandler handler) throws ScmException
    {
        assertRevisionArgValid(rev);
        CvsRevision cvsRev = convertRevision(rev);

        if (!isUpdateSupported(cvsRev))
        {
            throw new ScmException("The cvs server does not support updating to a specified date on a branch.");
        }

        // we can not run an update from the base directory, even though this is where the checkout occured.
        // Checkout will checkout into the current directory, but not generate a ./CVS directory.  For that, we need to
        // go into the sub directories.  So, if we have sub directories that contain a CVS directory, we have a local
        // working copy, and should run an update from WITHIN THOSE DIRECTORIES.  If not, then we can run a checkout
        // When will there be multiple directories? When we are dealing with an &module.
        File[] workingDirs = getSubdirectoriesContainingCvsDirectories(context.getWorkingDir());
        if (workingDirs.length > 0)
        {
            for (File workingDir : workingDirs)
            {
                core.update(workingDir, cvsRev, handler);
            }
        }
        return rev;
    }

    private boolean isUpdateSupported(CvsRevision rev) throws ScmException
    {
        if (StringUtils.stringSet(rev.getBranch()) && rev.getDate() != null)
        {
            // can only update to a specified date on a branch on some cvs servers.
            String version = core.version();
            return CvsServerCapabilities.supportsDateRevisionOnBranch(version);
        }
        return true;
    }

    public InputStream retrieve(ScmContext context, String path, Revision revision) throws ScmException
    {
        if (!StringUtils.stringSet(path))
        {
            throw new IllegalArgumentException("You need to specify a file to checkout.");
        }

        final File tmpDir[] = new File[1];
        try
        {
            tmpDir[0] = createTemporaryDirectory();

            core.checkout(tmpDir[0], path, convertRevision(revision), null);

            // read checked out file.
            File checkedOutFile = new File(tmpDir[0], path);
            if (!checkedOutFile.exists())
            {
                throw new ScmException("Unable to checkout file '" + path + "' from cvs[" + getRoot() + "].");
            }

            return new FileInputStream(checkedOutFile) {
                @Override
                public void close() throws IOException
                {
                    try
                    {
                        super.close();
                    }
                    finally
                    {
                        removeTempDir(tmpDir[0]);
                    }
                }
            };
        }
        catch (IOException e)
        {
            LOG.severe(e);
            throw new ScmException("Unable to retrieve requested file: " + path, e);
        }
        finally
        {
            removeTempDir(tmpDir[0]);
        }
    }

    private void removeTempDir(File dir)
    {
        if (dir != null)
        {
            try
            {
                FileSystemUtils.rmdir(dir);
            }
            catch (IOException e)
            {
                LOG.severe("Failed to remove temporary directory: " + e.getMessage());
            }
        }
    }

    public List<Changelist> getChanges(ScmContext context, Revision from, Revision to) throws ScmException
    {
        // assert that the branch for both revisions is the same. We do not support retrieving
        // differences across multiple branches/revisions. For practical reasons, we do not need to...

        List<LogInformation> info = new LinkedList<LogInformation>();

        for (String module : modules)
        {
            info.addAll(core.rlog(module, convertRevision(from), convertRevision(to)));
        }

        LogInformationAnalyser analyser = new LogInformationAnalyser(CVSRoot.parse(root));

        CvsRevision cvsFrom = convertRevision(from);
        CvsRevision cvsTo = convertRevision(to);

        String branch = (from != null) ? cvsFrom.getBranch() : (to != null) ? cvsTo.getBranch() : null;
        List<Changelist> changes = analyser.extractChangelists(info, branch);

        // process excludes from the changelist.
        changes = ScmUtils.filter(changes, new FilterPathsPredicate(includedPaths, excludedPaths));
        if (changes.size() == 0)
        {
            return changes;
        }

        // remove the module name from the start of the changes, and normalize.
        List<Changelist> fixedChangelists = new LinkedList<Changelist>();
        for (Changelist changelist : changes)
        {
            Changelist fixedChangelist = new Changelist(
                    changelist.getRevision(),
                    changelist.getTime(),
                    changelist.getAuthor(),
                    changelist.getComment(),
                    newArrayList(transform(changelist.getChanges(), new Function<FileChange, FileChange>()
                    {
                        public FileChange apply(FileChange change)
                        {
                            // a) strip off the leading /.
                            String filename = change.getPath();
                            if (filename.startsWith("/"))
                            {
                                filename = filename.substring(1);
                            }
                            // b) strip off the 'Attic' for dead files.  This may catch valid directories, but that is a less frequent case.
                            if (filename.contains("/Attic/"))
                            {
                                // looking for the attic parent directory...
                                // use the scmfile object to simplify the extraction of the 'Attic'
                                ScmFile file = new ScmFile(filename);
                                if (file.getParent() != null && file.getParent().endsWith("/Attic"))
                                {
                                    file = new ScmFile(file.getParentFile().getParentFile(), file.getName());
                                    filename = file.getPath();
                                }
                            }

                            return new FileChange(filename, change.getRevision(), change.getAction());
                        }
                    })));

            fixedChangelists.add(fixedChangelist);
        }

        changes = fixedChangelists;

        // ensure that the lower bound of the changes is excluded.
        Changelist firstChange = changes.get(0);
        if (firstChange.getRevision().equals(from))
        {
            return changes.subList(1, changes.size());
        }
        return changes;
    }

    public List<Revision> getRevisions(ScmContext context, Revision from, Revision to) throws ScmException
    {
        List<Changelist> changes = getChanges(null, from, to);
        Collections.sort(changes);

        List<Revision> result = new LinkedList<Revision>();
        for (Changelist c : changes)
        {
            result.add(c.getRevision());
        }

        return result;
    }

    public Revision getLatestRevision(ScmContext context) throws ScmException
    {
        // The latest revision is determined by running successive rlog commands.  Each time we go back a little
        // bit further in the hope that we encounter a change.  When we detect that change, we have the latest revision.
        // The time intervals for the checks are selected in an attempt to reduce the amount of network traffic. RLog
        // can be very expensive if there are a large number of changes. So, for high volume repositories, we would
        // expect recent changes.  For lower volume repositories, we would expect spread further apart. We stop looking
        // once we get to two years ago without any changes.

        Calendar cal = Calendar.getInstance();

        Calendar twoYearsAgo = Calendar.getInstance();
        twoYearsAgo.add(Calendar.YEAR, -2);

        int increment = 1;
        while (true)
        {
            cal.add(Calendar.DAY_OF_YEAR, -increment);

            CvsRevision since = new CvsRevision("", branch, "", cal.getTime());
            List<Revision> revisions = getRevisions(null, convertRevision(since), null);
            if (revisions.size() > 0)
            {
                return revisions.get(revisions.size() - 1);
            }

            // We have checked more than two years back, and still no changes. We
            // need to stop somewhere, so here is as good as anywhere.
            if (cal.compareTo(twoYearsAgo) < 0)
            {
                return convertRevision(new CvsRevision("", branch, "", cal.getTime()));
            }

            if (increment < 64)
            {
                increment = increment * 2;
            }
        }
    }

    public List<ScmFile> browse(ScmContext context, String path, Revision revision) throws ScmException
    {
        List<ScmFile> listing = new LinkedList<ScmFile>();

        for (RlsInfo info : core.list(path))
        {
            listing.add(new ScmFile(info.getModule(), info.getName(), info.isDirectory()));
        }

        return listing;
    }

    /**
     * Configure the temporary space root. This defaults to the users temporary directories.
     *
     * @param file directory to use for temporary space
     */
    public void setTemporarySpace(File file)
    {
        this.tmpSpace = file;
    }

    /**
     * Check that the module is valid.
     *
     * @throws ScmException on error
     */
    private void checkModuleIsValid() throws ScmException
    {
        File tmpDir = null;
        try
        {
            tmpDir = createTemporaryDirectory();

            String currentModule = null;
            try
            {
                // non - recursive.
                for (String module : modules)
                {
                    currentModule = module;
                    core.checkout(tmpDir, currentModule, null, false, null);
                }
            }
            catch (ScmException e)
            {
                throw new ScmException("Failed to locate the module '" + currentModule + "'");
            }
        }
        catch (IOException e)
        {
            throw new ScmException(e);
        }
        finally
        {
            // and lets not forget to clean up after ourselves.
            try
            {
                FileSystemUtils.rmdir(tmpDir);
            }
            catch (IOException e)
            {
                // Ignore.
            }
        }
    }

    private File createTemporaryDirectory() throws IOException
    {
        return FileSystemUtils.createTempDir("cvs", "checkout", tmpSpace);
    }

    public static CvsRevision convertRevision(Revision revision)
    {
        if (revision == null)
        {
            return null;
        }
        try
        {
            return new CvsRevision(revision.getRevisionString());
        }
        catch (ScmException e)
        {
            return null;
        }
    }

    public static Revision convertRevision(CvsRevision revision)
    {
        if (revision == null)
        {
            return null;
        }
        return new Revision(revision.getRevisionString());
    }

    /**
     * Throw an IllegalArgumentException if the revision is null.
     *
     * @param r the revision to check
     */
    private void assertRevisionArgValid(Revision r)
    {
        if (r == null)
        {
            throw new IllegalArgumentException("Revision is a required argument.");
        }
    }

}
