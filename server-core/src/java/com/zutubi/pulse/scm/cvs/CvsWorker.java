package com.zutubi.pulse.scm.cvs;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.CvsRevision;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.cvs.client.CvsClient;
import com.zutubi.pulse.scm.cvs.client.LogCommandListener;
import com.zutubi.pulse.scm.cvs.client.LogDirectoryBuilder;
import com.zutubi.pulse.scm.cvs.client.VersionCommand;
import com.zutubi.pulse.util.logging.Logger;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.lib.cvsclient.command.log.RlogCommand;
import org.netbeans.lib.cvsclient.command.tag.RtagCommand;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <class-comment/>
 */
public class CvsWorker
{
    private static final Logger LOG = Logger.getLogger(CvsWorker.class);

    private static final SimpleDateFormat SERVER_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    static
    {
        SERVER_DATE.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private String module;
    private String branch;
    private String root;
    private String password;

    private CvsClient client;
    private LogAnalyser analyser;

    /**
     * Set the cvs root
     * @param root
     */
    public void setRoot(String root)
    {
        this.root = root;
    }

    /**
     * Set the cvs module.
     *
     * @param module
     */
    public void setModule(String module)
    {
        this.module = checkModule(module);
    }

    /**
     * Set the cvs branch
     *
     * @param branch
     */
    public void setBranch(String branch)
    {
        this.branch = branch;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getModule()
    {
        return module;
    }

    public String getBranch()
    {
        return branch;
    }

    public String getRoot()
    {
        return root;
    }

    public String getPassword()
    {
        return password;
    }

    public void testConnection() throws SCMException
    {
        getClient().testConnection();
    }

    public String getServerVersion() throws SCMException
    {
        VersionCommand versionCommand = newVersionCommand();
        if (!getClient().executeCommand(versionCommand))
        {
            throw new SCMException("failed to retrieve the cvs server version details.");
        }
        return versionCommand.getVersion();
    }

    /**
     * @throws SCMException
     */
    public CvsRevision getLatestChange(String uid) throws SCMException
    {
        LOG.entering();

        // The latest change in a cvs repository is located by taking time x, and checking if
        // there have been any changes since that time. We jump through hoops (as mentioned below)
        // to handle possible time differences between the local and remote server machines. If
        // times were in sync, then the latest revision would be now. However, since times are not
        // in sync, we go back a few hours and have a look. 

        // We jump through hoops to handle the possible time difference between the hosts.

        Calendar cal = Calendar.getInstance();
        for (int hour = 1; hour < 24; hour = hour * 2)
        {
            // the longer its been without a change, the longer bigger the jumps we
            // can take since the repo is less and less used..
            cal.add(Calendar.HOUR, -1 * hour);

            CvsRevision since = new CvsRevision("", branch, "", cal.getTime());
            CvsRevision latest = getLatestChange(uid, since);
            if (latest != null)
            {
                return latest;
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
     * @param since
     * @throws SCMException
     */
    public CvsRevision getLatestChange(String uid, CvsRevision since) throws SCMException
    {
        LOG.entering();
        CvsClient client = getClient();
        LogAnalyser analyser = getAnalyser(uid);

        RlogCommand log = newRlogCommand();

        log.setDateFilter(SERVER_DATE.format(since.getDate()) + "<");

        List<LogInformation> response = new LinkedList<LogInformation>();

        client.executeCommand(log, new LogCommandListener(response));

        Date date = analyser.latestUpdate(response);
        CvsRevision result = null;
        if (date != null)
        {
            result = new CvsRevision("", branch, "", date);
        }
        LOG.exiting(result);
        return result;
    }

    public List<Changelist> getChangesBetween(String uid, CvsRevision from, CvsRevision to) throws SCMException
    {
        LOG.entering();

        // branches variables should all be the same. if not, then we are dealing with tags
        // and should be handled.

        // retrieve the log info for all of the files that have been modified.
        RlogCommand rlog = newRlogCommand();

        String dateFilter = "";
        String del = "<=";
        if (from != null && from.getDate() != null)
        {
            dateFilter = SERVER_DATE.format(from.getDate()) + del;
            del = "";
        }
        if (to != null && to.getDate() != null)
        {
            dateFilter += del + SERVER_DATE.format(to.getDate());
        }
        if (TextUtils.stringSet(dateFilter))
        {
            rlog.setDateFilter(dateFilter);
        }

        List<LogInformation> response = new LinkedList<LogInformation>();
        getClient().executeCommand(rlog, new LogCommandListener(response));

        List<Changelist> changelists = getAnalyser(uid).extract(response);
        LOG.exiting(changelists.size());
        return changelists;
    }

    /**
     * Execute a cvs checkout into the given directory.
     *
     */
    public CvsRevision checkout(File workdir, CvsRevision revision) throws SCMException
    {
        LOG.entering();
        CheckoutCommand checkout = newCheckoutCommand(revision);

        CvsClient client = getClient();
        client.setLocalPath(workdir);

        client.executeCommand(checkout);
        LOG.exiting(revision);
        return revision;
    }

    /**
     * Checkout the requested file / directory only.
     *
     */
    public CvsRevision checkout(File workdir, CvsRevision revision, String file) throws SCMException
    {
        LOG.entering();
        CheckoutCommand checkout = newCheckoutCommand(revision);
        checkout.setRecursive(false);
        checkout.setModule(file);

        // the revisions branch/tag overrides the default branch.
        if (TextUtils.stringSet(revision.getBranch()))
        {
            checkout.setCheckoutByRevision(revision.getBranch());
        }

        CvsClient client = getClient();
        client.setLocalPath(workdir);

        client.executeCommand(checkout);
        LOG.exiting(revision);
        return revision;
    }

    /**
     * The update command updates the configured module in the specified working directory.
     * An update requires that a checkout is executed to the same working directory.
     *
     * @param workdir
     * @param byDate
     * @throws SCMException
     */
    public void update(File workdir, CvsRevision byDate) throws SCMException
    {
        LOG.entering();
        UpdateCommand update = newUpdateCommand(byDate);
        CvsClient client = getClient();
        client.setLocalPath(new File(workdir, module));
        client.executeCommand(update);
        LOG.exiting();
    }

    public List<String> getListing() throws SCMException
    {
        LOG.entering();

        RlogCommand log = newRlogCommand();
        log.setDateFilter(SERVER_DATE.format(new Date()) + "<");

        LogDirectoryBuilder builder = new LogDirectoryBuilder();
        log.setBuilder(builder);

        getClient().executeCommand(log);

        List<String> directories = builder.getDirectories();
        LOG.exiting(directories.size());
        return directories;
    }

    /**
     *
     */
    private CvsClient getClient()
    {
        if (client == null)
        {
            client = new CvsClient(root);
            client.setPassword(password);
        }
        return client;
    }

    /**
     *
     */
    private LogAnalyser getAnalyser(String uid)
    {
        if (analyser == null)
        {
            analyser = new LogAnalyser(uid, getClient().getRoot());
        }
        return analyser;
    }

    /**
     * Check the value of the module string.
     *
     * @param module
     */
    private String checkModule(String module)
    {
        if (!TextUtils.stringSet(module))
        {
            throw new IllegalArgumentException("Command requires a module.");
        }

        // HACK: cvs client has trouble with absolute references, hanging if they are invalid.
        // Therefore, do not allow them.
        while (module.startsWith("/"))
        {
            module = module.substring(1);
        }
        return module;
    }

    /**
     * Create a new default rlog command.
     */
    private RlogCommand newRlogCommand()
    {
        RlogCommand log = new RlogCommand();
        log.setModule(module);

        // ensure that we get the information back in a timely fashion. Without this,
        // we receive a full log which takes too long to come across the wire.
        log.setSuppressHeader(true);

        // configure branch.
        if (TextUtils.stringSet(branch))
        {
            log.setRevisionFilter(branch);
        }
        else
        {
            log.setDefaultBranch(true); // work with head.
        }
        return log;
    }

    /**
     * Create a new default checkout command.
     */
    private CheckoutCommand newCheckoutCommand(CvsRevision revision)
    {
        CheckoutCommand checkout = new CheckoutCommand();
        checkout.setModule(module);
        checkout.setRecursive(true);

        // bind the checkout to the specified tag.
        if (TextUtils.stringSet(branch))
        {
            checkout.setCheckoutByRevision(branch);
        }

        if (revision.getDate() != null)
        {
            checkout.setCheckoutByDate(SERVER_DATE.format(revision.getDate()));
        }

        return checkout;
    }

    private UpdateCommand newUpdateCommand(CvsRevision revision)
    {
        UpdateCommand update = new UpdateCommand();
        update.setPruneDirectories(true);
        update.setBuildDirectories(true);
        update.setResetStickyOnes(true);

        if (TextUtils.stringSet(branch))
        {
            update.setUpdateByRevision(branch);
        }
        if (revision.getDate() != null)
        {
            update.setUpdateByDate(SERVER_DATE.format(revision.getDate()));
        }
        return update;
    }

    private VersionCommand newVersionCommand()
    {
        return new VersionCommand();
    }

    public void tag(CvsRevision revision, String name, boolean moveExisting) throws SCMException
    {
        LOG.entering();

        RtagCommand tag = new RtagCommand();
        tag.setModules(new String[] { module });

        if(TextUtils.stringSet(branch))
        {
            tag.setTagByRevision(branch);
        }
        
        if (revision.getDate() != null)
        {
            tag.setTagByDate(SERVER_DATE.format(revision.getDate()));
        }

        tag.setTag(name);
        tag.setOverrideExistingTag(moveExisting);

        CvsClient client = getClient();
        client.executeCommand(tag);

        LOG.exiting();
    }
}
