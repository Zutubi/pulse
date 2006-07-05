package com.zutubi.pulse.scm.cvs;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.CvsRevision;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.filesystem.remote.CachingRemoteFile;
import com.zutubi.pulse.model.Cvs;
import com.zutubi.pulse.scm.*;
import com.zutubi.pulse.scm.cvs.client.CvsClient;
import com.zutubi.pulse.util.Constants;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.util.logging.Logger;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.command.log.LogInformation;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * The Cvs Server provides all interactions with a cvs repository.
 */
public class CvsServer extends CachingSCMServer
{
    private File tmpSpace;

    private static final Logger LOG = Logger.getLogger(CvsServer.class);

    private CvsWorker cvs;

    /**
     * A list of ant style path expressions that define what should be excluded from being considered as a change.
     */
    private List<String> excludedPaths = new LinkedList<String>();

    public CvsServer(String root, String module, String password, String branch)
    {
        cvs = new CvsWorker();
        cvs.setBranch(branch);
        cvs.setModule(module);
        cvs.setRoot(root);
        cvs.setPassword(password);
    }

    public CvsServer(Cvs cvs)
    {
        this(cvs.getRoot(), cvs.getModule(), cvs.getPassword(), cvs.getBranch());
        setExcludedPaths(cvs.getFilteredPaths());
    }

    public void setExcludedPaths(List<String> excluded)
    {
        this.excludedPaths = excluded;
    }

    /**
     * Get access to the servers properties. These include:
     * <ul>
     * <li>location: the location property.</li>
     * <li>version: the version of the remote server.</li>
     * </ul>
     *
     * @return
     *
     * @see #getLocation()
     *
     * @throws SCMException
     */
    public Map<String, String> getServerInfo() throws SCMException
    {
        Map<String, String> info = new TreeMap<String, String>();
        info.put("location", getLocation());
        info.put("version", cvs.getServerVersion());
        return info;
    }

    /**
     * Returns the unique identifier for this scm server. For CVS servers, this is the cvs root.
     *
     * @see com.zutubi.pulse.scm.SCMServer#getUid()
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
        return cvs.getRoot();
    }

    /**
     * Get the configured cvs password property.
     *
     * @return connection password.
     */
    public String getPassword()
    {
        return cvs.getPassword();
    }

    /**
     * Get the configured cvs module property.
     *
     * @return module
     */
    public String getModule()
    {
        return cvs.getModule();
    }

    /**
     * Get the branch to which this cvs server instance is bound.
     *
     * @return branch name.
     */
    public String getBranch()
    {
        return cvs.getBranch();
    }

    /**
     * Run some diagnostics on the cvs configuration.
     *
     * @throws SCMException is thrown if there is a problem with the server connection or configuration that
     * prevents us from querying the cvs repository.
     */
    public void testConnection() throws SCMException
    {
        // Check the connection to the cvs repository.  This covers the cvs root and authentication.
        cvs.testConnection();

        // Check that the module is valid.
        checkModuleIsValid();
    }

    /**
     * Update the working directory to the specified revision.  It is required that the working
     * directory has a local checkout that can be updated.
     *
     * @param workingDirectory
     * @param rev
     */
    public void update(File workingDirectory, Revision rev) throws SCMException
    {
        assertRevisionArgValid(rev);
        cvs.update(workingDirectory, (CvsRevision) rev);
    }

    /**
     * Returns true since this scm implementation supports the update command.
     *
     * @return true.
     */
    public boolean supportsUpdate()
    {
        return true;
    }

    public void tag(Revision revision, String name, boolean moveExisting) throws SCMException
    {
        assertRevisionArgValid(revision);
        cvs.tag((CvsRevision) revision, name, moveExisting);
    }

    public Revision checkout(long id, File toDirectory, Revision revision, List<Change> changes) throws SCMException
    {
        assertRevisionArgValid(revision);

        cvs.checkout(toDirectory, (CvsRevision)revision);

        return revision;
    }

    public String checkout(long id, Revision revision, String file) throws SCMException
    {
        if (!TextUtils.stringSet(file))
        {
            throw new IllegalArgumentException("You need to specify a file to checkout.");
        }

        if (revision == null)
        {
            revision = getLatestRevision();
        }

        File tmpDir = null;
        try
        {
            tmpDir = createTemporaryDirectory();

            cvs.checkout(tmpDir, (CvsRevision)revision, file);

            // read checked out file.
            File checkedOutFile = new File(tmpDir, file);
            if (!checkedOutFile.exists())
            {
                throw new SCMException("Unable to checkout file '" + file + "' from cvs[" + getRoot() + "].");
            }
            return IOUtils.fileToString(checkedOutFile);
        }
        catch (IOException e)
        {
            LOG.severe(e);
            throw new SCMException("Unable to retrieve requested file: " + file, e);
        }
        finally
        {
            if (!FileSystemUtils.removeDirectory(tmpDir))
            {
                LOG.severe("failed to remove temporary directory " + tmpDir);
            }
        }
    }

    /**
     * Retreive the list of changes between the two specified revisions.
     *
     * @param from
     * @param to
     *
     * @param paths are ignored.
     *
     * @return a list of changelist instances.
     *
     * @throws SCMException
     */
    public List<Changelist> getChanges(Revision from, Revision to, String ...paths) throws SCMException
    {
        // assert that the branch for both revisions is the same. We do not support retrieving
        // differences across multiple branches/revisions. For practical reasons, we do not need to...

        List<Changelist> changes = cvs.getChangesBetween(getUid(), (CvsRevision)from, (CvsRevision)to);

        // process excludes from the changelist.
        changes = filterExcludes(changes, new ScmFilepathFilter(excludedPaths));

        if (changes.size() == 0)
        {
            return changes;
        }

        // ensure that the lower bound of the changes is excluded.
        Changelist firstChange = changes.get(0);
        if (firstChange.getRevision().compareTo(from) == 0)
        {
            return changes.subList(1, changes.size());
        }
        return changes;
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
        for(Changelist c: changes)
        {
            result.add(c.getRevision());
        }

        return result;
    }

    private List<Changelist> filterExcludes(List<Changelist> changelists, FilepathFilter filter)
    {
        Iterator<Changelist> changelist = changelists.iterator();
        while (changelist.hasNext())
        {
            Changelist ch = changelist.next();
            Iterator<Change> i = ch.getChanges().iterator();
            while (i.hasNext())
            {
                Change c = i.next();
                if (filter != null && !filter.accept(c.getFilename()))
                {
                    i.remove();
                }
            }
            if (ch.getChanges().size() == 0)
            {
                changelist.remove();
            }
        }
        return changelists;
    }

    /**
     * This method checks to see if there have been any changes to the scm system since the
     * specified revision.
     *
     * @param since
     * @return true if a change has been detected, false otherwise.
     * @throws SCMException
     */
    public boolean hasChangedSince(Revision since) throws SCMException
    {
        if (since.getDate() == null)
        {
            throw new IllegalArgumentException("since revision date can not be null.");
        }

        List<Changelist> changelists = getChanges(since, null);
        changelists = filterExcludes(changelists, new ScmFilepathFilter(excludedPaths));
        return changelists.size() > 0;
    }

    public CvsRevision getLatestRevision() throws SCMException
    {
        return cvs.getLatestChange(getUid());
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
     * The configuration manager is required to provide access to the system temporary root directory.
     *
     * @param configurationManager
     *
     * @see CvsServer#setTemporarySpace(java.io.File)
     */
    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        setTemporarySpace(configurationManager.getSystemPaths().getTmpRoot());
    }

    public void populate(SCMFileCache.CacheItem item) throws SCMException
    {
        item.cachedRevision = getLatestRevision();
        item.cachedListing = new TreeMap<String, CachingRemoteFile>();

        CvsClient client = new CvsClient(getRoot());
        client.setPassword(getPassword());
        List<LogInformation> logs = client.rlog(getModule(), null, null, null, false);

        CVSRoot root = CVSRoot.parse(getRoot());

        CachingRemoteFile rootFile = new CachingRemoteFile("", true, null, "");
        item.cachedListing.put("", rootFile);

        for (LogInformation log : logs)
        {
            String filename = log.getRepositoryFilename();

            // remove the ,v
            if (filename.endsWith(",v"))
            {
                filename = filename.substring(0, filename.length() - 2);
            }

            // remove the repo root.
            if (filename.startsWith(root.getRepository()))
            {
                filename = filename.substring(root.getRepository().length());
            }

            // break this up into files and directories.
            addToCache(filename, rootFile, item);
        }
    }

    public boolean requiresRefresh(Revision revision) throws SCMException
    {
        if(System.currentTimeMillis() - revision.getDate().getTime() > Constants.MINUTE * 5)
        {
            return super.requiresRefresh(revision);
        }
        else
        {
            return false;
        }
    }

    /**
     * Check that the module is valid.
     *
     * @throws SCMException
     */
    private void checkModuleIsValid() throws SCMException
    {
        File tmpDir = null;
        try
        {
            tmpDir = createTemporaryDirectory();

            try
            {
                cvs.checkout(tmpDir, CvsRevision.HEAD, getModule());
            }
            catch (SCMException e)
            {
                throw new SCMException("Failed to locate the module " + getModule());
            }
        }
        catch (IOException e)
        {
            throw new SCMException(e);
        }
        finally
        {
            // and lets not forget to clean up after ourselves.
            FileSystemUtils.removeDirectory(tmpDir);
        }
    }

    private File createTemporaryDirectory() throws IOException
    {
        return FileSystemUtils.createTempDirectory("cvs", "checkout", tmpSpace);
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
        if (!(r instanceof CvsRevision))
        {
            throw new IllegalArgumentException("Unsupported revision type: " + r.getClass() + ".");
        }
    }

}
