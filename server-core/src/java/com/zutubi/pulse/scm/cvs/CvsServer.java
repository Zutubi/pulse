package com.zutubi.pulse.scm.cvs;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.filesystem.remote.CachingRemoteFile;
import com.zutubi.pulse.scm.*;
import com.zutubi.pulse.scm.cvs.client.CvsClient;
import com.zutubi.pulse.scm.cvs.client.LogInformationAnalyser;
import com.zutubi.pulse.util.Constants;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.util.logging.Logger;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.command.log.LogInformation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * The Cvs Server provides all interactions with a cvs repository.
 */
public class CvsServer extends CachingSCMServer
{
    private File tmpSpace;

    private static final Logger LOG = Logger.getLogger(CvsServer.class);

    private CvsClient client;

    private String module;
    private String branch;
    private String root;
    private String password;

    /**
     * A list of ant style path expressions that define what should be excluded from being considered as a change.
     */
    private List<String> excludedPaths = new LinkedList<String>();

    public CvsServer(String root, String module, String password, String branch)
    {
        client = new CvsClient();
        client.setRoot(CVSRoot.parse(root));
        client.setPassword(password);

        this.module = module;
        this.branch = branch;
        this.root = root;
        this.password = password;
    }

    public CvsServer(String root, String module, String password, String branch, List<String> filteredPaths, File tempDir)
    {
        this(root, module, password, branch);
        setExcludedPaths(filteredPaths);
        setTemporarySpace(tempDir);
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
     * @return a map of key value pairs representing the server information.
     *
     * @see #getLocation()
     *
     * @throws com.zutubi.pulse.scm.SCMException
     */
    public Map<String, String> getServerInfo() throws SCMException
    {
        Map<String, String> info = new TreeMap<String, String>();
        info.put("location", getLocation());
        info.put("version", client.version());
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
     * @throws SCMException is thrown if there is a problem with the server connection or configuration that
     * prevents us from querying the cvs repository.
     */
    public void testConnection() throws SCMException
    {
        // Check the connection to the cvs repository.  This covers the cvs root and authentication.
        client.testConnection();

        // Check that the module is valid.
        checkModuleIsValid();
    }

    /**
     * Update the working directory to the specified revision.  It is required that the working
     * directory has a local checkout that can be updated.
     *
     * @param workingDirectory
     * @param rev
     * @param handler
     */
    public void update(String id, File workingDirectory, Revision rev, SCMCheckoutEventHandler handler) throws SCMException
    {
        assertRevisionArgValid(rev);
        client.update(workingDirectory, (CvsRevision) rev, handler);
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
        client.tag(module, (CvsRevision) revision, name, moveExisting);
    }

    public void writeConnectionDetails(File outputDir) throws SCMException, IOException
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

    public FileRevision getFileRevision(String path, Revision repoRevision)
    {
        return null;
    }

    public Revision checkout(String id, File toDirectory, Revision revision, SCMCheckoutEventHandler handler) throws SCMException
    {
        assertRevisionArgValid(revision);
        client.checkout(toDirectory, module, (CvsRevision)revision, handler);
        return revision;
    }

    public String checkout(Revision revision, String file) throws SCMException
    {
        if (!TextUtils.stringSet(file))
        {
            throw new IllegalArgumentException("You need to specify a file to checkout.");
        }

        if (revision == null)
        {
            revision = CvsRevision.HEAD;
        }

        File tmpDir = null;
        try
        {
            tmpDir = createTemporaryDirectory();

            client.checkout(tmpDir, file, (CvsRevision)revision, null);

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
            if (!FileSystemUtils.rmdir(tmpDir))
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

        List<LogInformation> info = client.rlog(module, (CvsRevision)from, (CvsRevision)to);
        LogInformationAnalyser analyser = new LogInformationAnalyser(getUid(), CVSRoot.parse(root));

        String branch = (from != null) ? from.getBranch() : (to != null) ? to.getBranch() : null;
        List<Changelist> changes = analyser.extract(info, branch);

        // process excludes from the changelist.
        changes = filterExcludes(changes, new ScmFilepathFilter(excludedPaths));
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
        // The latest change in a cvs repository is located by taking time x, and checking if
        // there have been any changes since that time. We jump through hoops (as mentioned below)
        // to handle possible time differences between the local and remote server machines. If
        // times were in sync, then the latest revision would be now. However, since times are not
        // in sync, we go back a few hours and have a look.

        // We jump through hoops to handle the possible time difference between the hosts.

        LogInformationAnalyser analyser = new LogInformationAnalyser(getUid(), CVSRoot.parse(root));

        Calendar cal = Calendar.getInstance();
        for (int hour = 1; hour < 24; hour = hour * 2)
        {
            // the longer its been without a change, the longer bigger the jumps we
            // can take since the repo is less and less used..
            cal.add(Calendar.HOUR, -1 * hour);

            CvsRevision since = new CvsRevision("", branch, "", cal.getTime());

            Date latestUpdate = analyser.latestUpdate(client.rlog(module, since, null));
            if (latestUpdate != null)
            {
                // should we be returning the author and comment of the latest update as well?... probably :|
                return new CvsRevision("", branch, "", latestUpdate);
            }
        }

        // If the cvs server is ahead of this host, then any changes would have been picked
        // up if they occured.

        // Assuming that the time is no more then 24 hours behind, we can assume
        // that the latest calendar time will give us a reasonable starting point.

        // need to ensure that the specified date is server centric.
        CvsRevision result = new CvsRevision("", branch, "", cal.getTime());
        LOG.exiting(result);
        return result;
    }

    /**
     * Configure the temporary space root. This defaults to the users temporary directories.
     *
     */
    public void setTemporarySpace(File file)
    {
        this.tmpSpace = file;
    }

    public void populate(SCMFileCache.CacheItem item) throws SCMException
    {
        item.cachedRevision = getLatestRevision();
        item.cachedListing = new TreeMap<String, CachingRemoteFile>();

        List<LogInformation> logs = client.rlog(module, null, null, true);

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
                // non - recursive.
                client.checkout(tmpDir, module, CvsRevision.HEAD, false, null);
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
            FileSystemUtils.rmdir(tmpDir);
        }
    }

    private File createTemporaryDirectory() throws IOException
    {
        return FileSystemUtils.createTempDir("cvs", "checkout", tmpSpace);
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
