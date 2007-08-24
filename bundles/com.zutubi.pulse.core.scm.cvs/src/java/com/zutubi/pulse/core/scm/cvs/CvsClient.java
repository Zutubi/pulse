package com.zutubi.pulse.core.scm.cvs;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.scm.*;
import com.zutubi.pulse.core.scm.cvs.client.CvsCore;
import com.zutubi.pulse.core.scm.cvs.client.LogInformationAnalyser;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.util.CleanupInputStream;
import com.zutubi.util.IOUtils;
import com.zutubi.util.logging.Logger;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.command.log.LogInformation;

import java.io.File;
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
     * Get access to the servers properties. These include:
     * <ul>
     * <li>location: the location property.</li>
     * <li>version: the version of the remote server.</li>
     * </ul>
     *
     * @return a map of key value pairs representing the server information.
     *
     * @see #getLocation()
     *
     * @throws com.zutubi.pulse.core.scm.ScmException
     */
    public Map<String, String> getServerInfo() throws ScmException
    {
        Map<String, String> info = new TreeMap<String, String>();
        info.put("location", getLocation());
        info.put("version", core.version());
        return info;
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

    /**
     * Update the working directory to the specified revision.  It is required that the working
     * directory has a local checkout that can be updated.
     *
     * @param context
     * @param handler
     */
    public void update(ScmContext context, ScmEventHandler handler) throws ScmException
    {
        Revision rev = context.getRevision();
        assertRevisionArgValid(rev);
        core.update(context.getDir(), convertRevision(rev), handler);
    }

    public void tag(Revision revision, String name, boolean moveExisting) throws ScmException
    {
        assertRevisionArgValid(revision);
        core.tag(module, convertRevision(revision), name, moveExisting);
    }

    public List<ResourceProperty> getProperties(String id, File dir) throws ScmException
    {
        List<ResourceProperty> result = new ArrayList<ResourceProperty>(3);
        result.add(new ResourceProperty("cvs.root", root));
        if (branch != null)
        {
            result.add(new ResourceProperty("cvs.branch", branch));
        }
        result.add(new ResourceProperty("cvs.module", module));
        return result;
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
        assertRevisionArgValid(revision);
        core.checkout(context.getDir(), module, convertRevision(revision), handler);
        return revision;
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

        // ensure that the lower bound of the changes is excluded.
        Changelist firstChange = changes.get(0);
        if (firstChange.getRevision().getDate().equals(from.getDate()))
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
        // The latest change in a cvs repository is located by taking time x, and checking if
        // there have been any changes since that time. We jump through hoops (as mentioned below)
        // to handle possible time differences between the local and remote server machines. If
        // times were in sync, then the latest revision would be now. However, since times are not
        // in sync, we go back a few hours and have a look.

        // We jump through hoops to handle the possible time difference between the hosts.

        LogInformationAnalyser analyser = new LogInformationAnalyser(getUid(), CVSRoot.parse(root));

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);

        CvsRevision since = new CvsRevision("", branch, "", cal.getTime());

        Date latestUpdate = analyser.latestUpdate(core.rlog(module, since, null));
        if (latestUpdate != null)
        {
            // should we be returning the author and comment of the latest update as well?... probably :|
            return convertRevision(new CvsRevision("", branch, "", latestUpdate));
        }

        // If the cvs server is ahead of this host, then any changes would have been picked
        // up if they occured.

        // Assuming that the time is no more then 24 hours behind, we can assume
        // that the latest calendar time will give us a reasonable starting point.

        CvsRevision result = new CvsRevision("", branch, "", cal.getTime());
        LOG.exiting(result);
        return convertRevision(result);
    }

    public List<ScmFile> browse(String path) throws ScmException
    {
        return new LinkedList<ScmFile>();
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
