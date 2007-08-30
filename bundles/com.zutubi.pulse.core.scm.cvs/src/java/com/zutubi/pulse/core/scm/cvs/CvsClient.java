package com.zutubi.pulse.core.scm.cvs;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.scm.*;
import com.zutubi.pulse.core.scm.cvs.client.CvsCore;
import com.zutubi.pulse.core.scm.cvs.client.LogInformationAnalyser;
import com.zutubi.pulse.core.scm.cvs.client.commands.RlsInfo;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.util.CleanupInputStream;
import com.zutubi.util.IOUtils;
import com.zutubi.util.logging.Logger;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.command.log.LogInformation;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * The CvsClient provides all interactions with a cvs repository.
 */
public class CvsClient implements ScmClient, DataCacheAware
{
    public static final String TYPE = "cvs";

    private File tmpSpace;

    private static final Logger LOG = Logger.getLogger(CvsClient.class);

    private CvsCore core;

    private String module;
    private String branch;
    private String root;
    private String password;

    private Map<Object, Object> cache;

    /**
     * A list of ant style path expressions that define what should be excluded from being considered as a change.
     */
    private List<String> excludedPaths = new LinkedList<String>();

    public CvsClient(String root, String module, String password, String branch)
    {
        core = new CvsCore();
        core.setRoot(CVSRoot.parse(root));
        core.setPassword(password);

        this.module = module;
        
        // CIB-911: ensure that we trim any whitespace from the module, else the cvs command will return false.
        if (module != null)
        {
            this.module = this.module.trim();
        }
        
        this.branch = branch;
        this.root = root;
        this.password = password;
    }

    public CvsClient(String root, String module, String password, String branch, List<String> filteredPaths, File tempDir)
    {
        this(root, module, password, branch);
        setExcludedPaths(filteredPaths);
        setTemporarySpace(tempDir);
    }

    public void setExcludedPaths(List<String> excluded)
    {
        this.excludedPaths = excluded;
    }

    public Set<ScmCapability> getCapabilities()
    {
        return new HashSet<ScmCapability>(Arrays.asList(ScmCapability.values()));
    }

    /**
     * Returns the unique identifier for this scm server. For CVS servers, this is the cvs root.
     *
     * @see com.zutubi.pulse.core.scm.ScmClient#getUid()
     */
    public String getUid()
    {
        return getRoot();
    }

    public String getLocation()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getRoot()).append("[").append(getModule()).append("]");
        return buffer.toString();
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
     * @throws ScmException is thrown if there is a problem with the server connection or configuration that
     * prevents us from querying the cvs repository.
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

    public void tag(Revision revision, String name, boolean moveExisting) throws ScmException
    {
        assertRevisionArgValid(revision);
        core.tag(module, convertRevision(revision), name, moveExisting);
    }

    private void writePropertiesToContext(ScmContext context) throws ScmException
    {
        context.addProperty("cvs.root", root);
        if (branch != null)
        {
            context.addProperty("cvs.branch", branch);
        }
        context.addProperty("cvs.module", module);
    }

    public void storeConnectionDetails(File outputDir) throws ScmException, IOException
    {
        Properties props = new Properties();
        props.put("root", root);
        if(branch != null)
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

    public FileStatus.EOLStyle getEOLPolicy()
    {
        return FileStatus.EOLStyle.BINARY;
    }

    public Revision getRevision(String revision) throws ScmException
    {
        CvsRevision cvsRevision = new CvsRevision(revision);
        if(cvsRevision.getBranch() == null)
        {
            // As this is a user-specified value, we set the right branch for
            // them when it is left unspecified.
            cvsRevision.setBranch(branch);
        }
        
        return convertRevision(cvsRevision);
    }

    public Revision checkout(ScmContext context, ScmEventHandler handler) throws ScmException
    {
        Revision revision = context.getRevision();
        if (revision == Revision.HEAD)
        {
            // FIXME: it would be good to be able to avoid this call to get the latest revision and
            // simply use the information from the checkout / update
            // It would be nice to have the revision that we checkout/update to be returned by the cvscore
            // and then return that revision from this method.
            revision = getLatestRevision();
        }

        writePropertiesToContext(context);
        
        core.checkout(context.getDir(), module, convertRevision(revision), handler);
        
        return revision;
    }

    /**
     * Update the working directory to the specified revision.  It is required that the working
     * directory has a local checkout that can be updated.
     *
     * @param context
     * @param handler
     */
    public Revision update(ScmContext context, ScmEventHandler handler) throws ScmException
    {
        Revision rev = context.getRevision();
        assertRevisionArgValid(rev);
        writePropertiesToContext(context);

        // we can not run an update from the base directory, even though this is where the checkout occured.
        // Checkout will checkout into the current directory, but not generate a ./CVS directory.  For that, we need to
        // go into the sub directories.  So, if we have sub directories that contain a CVS directory, we have a local
        // working copy, and should run an update from WITHIN THOSE DIRECTORIES.  If not, then we can run a checkout
        // When will there be multiple directories? When we are dealing with an &module.
        File[] workingDirs = getSubdirectoriesContainingCvsDirectories(context.getDir());
        if (workingDirs.length > 0)
        {
            for (File workingDir : workingDirs)
            {
                core.update(workingDir, convertRevision(rev), handler);
            }
        }
        return rev;
    }

    public InputStream retrieve(String path, Revision revision) throws ScmException
    {
        if (!TextUtils.stringSet(path))
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

            FileInputStream fis = new FileInputStream(checkedOutFile);
            return new CleanupInputStream(fis, new CleanupInputStream.CleanupCallback()
            {
                public void execute()
                {
                    if (!FileSystemUtils.rmdir(tmpDir[0]))
                    {
                        LOG.severe("failed to remove temporary directory " + tmpDir[0]);
                    }
                }
            });
        }
        catch (IOException e)
        {
            LOG.severe(e);
            throw new ScmException("Unable to retrieve requested file: " + path, e);
        }
        finally
        {
            if (tmpDir[0] != null && !FileSystemUtils.rmdir(tmpDir[0]))
            {
                LOG.severe("failed to remove temporary directory " + tmpDir[0]);
            }
        }
    }

    public List<Changelist> getChanges(Revision from, Revision to) throws ScmException
    {
        // assert that the branch for both revisions is the same. We do not support retrieving
        // differences across multiple branches/revisions. For practical reasons, we do not need to...

        List<LogInformation> info = core.rlog(module, convertRevision(from), convertRevision(to));
        LogInformationAnalyser analyser = new LogInformationAnalyser(getUid(), CVSRoot.parse(root));

        String branch = (from != null) ? from.getBranch() : (to != null) ? to.getBranch() : null;
        List<Changelist> changes = analyser.extractChangelists(info, branch);

        // process excludes from the changelist.
        changes = ScmUtils.filterExcludes(changes, new ScmFilepathFilter(excludedPaths));
        if (changes.size() == 0)
        {
            return changes;
        }

        // remove the module name from the start of the changes, and normalize.
        List<Changelist> fixedChangelists = new LinkedList<Changelist>();
        for (Changelist changelist : changes)
        {
            Changelist fixedChangelist = new Changelist(changelist.getServerUid(), changelist.getRevision());
            for (Change change : changelist.getChanges())
            {
                // a) strip off the leading /.
                String filename = change.getFilename();
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
                Change fixedChange = new Change(filename, change.getRevisionString(), change.getAction());
                fixedChangelist.addChange(fixedChange);
            }
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

    public List<Revision> getRevisions(Revision from, Revision to) throws ScmException
    {
        List<Changelist> changes = getChanges(from, to);
        Collections.sort(changes, new Comparator<Changelist>()
        {
            public int compare(Changelist o1, Changelist o2)
            {
                return o1.getDate().compareTo(o2.getDate());
            }
        });

        List<Revision> result = new LinkedList<Revision>();
        for(Changelist c: changes)
        {
            result.add(c.getRevision());
        }

        return result;
    }

    /**
     * This method checks to see if there have been any changes to the scm system since the
     * specified revision.
     *
     * @param since
     * @return true if a change has been detected, false otherwise.
     * @throws ScmException
     */
    public boolean hasChangedSince(Revision since) throws ScmException
    {
        if (since.getDate() == null)
        {
            throw new IllegalArgumentException("since revision date can not be null.");
        }

        List<Changelist> changelists = getChanges(since, null);
        changelists = ScmUtils.filterExcludes(changelists, new ScmFilepathFilter(excludedPaths));
        return changelists.size() > 0;
    }

    public Revision getLatestRevision() throws ScmException
    {
        // The latest revision is determined by running successive rlog commands.  Each time we go back a little
        // bit further in the hope that we encounter a change.  When we detect that change, we have the latest revision.
        // The time intervals for the checks are selected in an attempt to reduce the amount of network traffic. RLog
        // can be very expensive if there are a large number of changes. So, for high volume repositories, we would
        // expect recent changes.  For lower volume repositories, we would expect spread further apart. We stop looking
        // once we get to two years ago without any changes.

        LogInformationAnalyser analyser = new LogInformationAnalyser(getUid(), CVSRoot.parse(root));

        Calendar cal = Calendar.getInstance();

        Calendar twoYearsAgo = Calendar.getInstance();
        twoYearsAgo.add(Calendar.YEAR, -2);

        LogInformationAnalyser.Revision latestUpdate;
        int increment = 1;
        while (true)
        {
            cal.add(Calendar.DAY_OF_YEAR, -increment);

            CvsRevision since = new CvsRevision("", branch, "", cal.getTime());
            latestUpdate = analyser.latestUpdate(core.rlog(module, since, null));
            if (latestUpdate != null)
            {
                return convertRevision(new CvsRevision(latestUpdate.getAuthor(), branch, latestUpdate.getMessage(), latestUpdate.getDate()));
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

    public List<ScmFile> browse(String path, Revision revision) throws ScmException
    {
        List<ScmFile> listing = new LinkedList<ScmFile>();

        for (RlsInfo info : core.list(path))
        {
            listing.add(new ScmFile(info.getModule(), info.getName(), info.isDirectory()));
        }

        return listing;
    }

    //---( data cache aware implementation )---

    public String getCacheId()
    {
        return getUid(); // unique to each external repository since we use the cache to store per server details.
    }

    public void setCache(Map<Object, Object> cache)
    {
        this.cache = cache;
    }

    /**
     * Configure the temporary space root. This defaults to the users temporary directories.
     *
     */
    public void setTemporarySpace(File file)
    {
        this.tmpSpace = file;
    }

    /**
     * Check that the module is valid.
     *
     * @throws ScmException
     */
    private void checkModuleIsValid() throws ScmException
    {
        File tmpDir = null;
        try
        {
            tmpDir = createTemporaryDirectory();

            try
            {
                // non - recursive.
                core.checkout(tmpDir, module, null, false, null);
            }
            catch (ScmException e)
            {
                throw new ScmException("Failed to locate the module " + getModule());
            }
        }
        catch (IOException e)
        {
            throw new ScmException(e);
        }
        finally
        {
            // and lets not forget to clean up after ourselves.
            FileSystemUtils.rmdir(tmpDir);
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
        return new CvsRevision(revision.getAuthor(), revision.getBranch(), revision.getComment(), revision.getDate());
    }

    public static Revision convertRevision(CvsRevision revision)
    {
        if (revision == null)
        {
            return null;
        }
        Revision newRevision = new Revision(revision.getAuthor(), revision.getComment(), revision.getDate(), revision.getRevisionString());
        newRevision.setBranch(revision.getBranch());
        return newRevision;
    }

    /**
     * Throw an IllegalArgumentException if either of the following are true:
     * <ul>
     * <li>The revision is null</li>
     * <li>The revision is not of type CvsRevision</li>
     * </ul>
     *
     * @param r
     */
    private void assertRevisionArgValid(Revision r)
    {
        if (r == null)
        {
            throw new IllegalArgumentException("Revision is a required argument.");
        }
    }

}
