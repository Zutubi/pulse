package com.zutubi.pulse.scm.cvs;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.core.model.CvsRevision;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.cvs.client.LogCommandListener;
import com.zutubi.pulse.scm.cvs.client.LogDirectoryBuilder;
import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.lib.cvsclient.command.log.RlogCommand;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;

import java.text.SimpleDateFormat;
import java.util.*;
import java.io.File;

/**
 * <class-comment/>
 */
public class CvsWorker
{
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
    public CvsRevision getLatestChange() throws SCMException
    {
        // We jump through hoops to handle the possible time difference between the hosts.

        Calendar cal = Calendar.getInstance();
        for (int hour = 1; hour < 24; hour = hour * 2)
        {
            // the longer its been without a change, the longer bigger the jumps we
            // can take since the repo is less and less used..
            cal.add(Calendar.HOUR, -1 * hour);

            CvsRevision since = new CvsRevision("", branch, "", cal.getTime());
            CvsRevision latest = getLatestChange(since);
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
        return new CvsRevision("", branch, "", cal.getTime());
    }

    /**
     * @param since
     * @throws SCMException
     */
    public CvsRevision getLatestChange(CvsRevision since) throws SCMException
    {
        CvsClient client = getClient();
        LogAnalyser analyser = getAnalyser();

        RlogCommand log = newRlogCommand();

        log.setDateFilter(SERVER_DATE.format(since.getDate()) + "<");

        List<LogInformation> response = new LinkedList<LogInformation>();

        client.executeCommand(log, new LogCommandListener(response));

        Date date = analyser.latestUpdate(response);
        if (date != null)
        {
            return new CvsRevision("", branch, "", date);
        }
        return null;
    }

    public List<Changelist> getChangesBetween(CvsRevision from, CvsRevision to) throws SCMException
    {
        // branches variables should all be the same. if not, then we are dealing with tags
        // and should be handled.

        // retrieve the log info for all of the files that have been modified.
        RlogCommand rlog = newRlogCommand();

        String dateFilter = "";
        String del = "<=";
        if (from.getDate() != null)
        {
            dateFilter = SERVER_DATE.format(from.getDate()) + del;
            del = "";
        }
        if (to.getDate() != null)
        {
            dateFilter += del + SERVER_DATE.format(to.getDate());
        }
        if (TextUtils.stringSet(dateFilter))
        {
            rlog.setDateFilter(dateFilter);
        }

        List<LogInformation> response = new LinkedList<LogInformation>();
        getClient().executeCommand(rlog, new LogCommandListener(response));

        return getAnalyser().extract(response);
    }

    /**
     * Execute a cvs checkout into the given directory.
     *
     */
    public CvsRevision checkout(File workdir, CvsRevision revision) throws SCMException
    {
        CheckoutCommand checkout = newCheckoutCommand(revision);

        CvsClient client = getClient();
        client.setLocalPath(workdir);

        client.executeCommand(checkout);

        return revision;
    }

    /**
     * Checkout the requested file / directory only.
     *
     */
    public CvsRevision checkout(File workdir, CvsRevision revision, String file) throws SCMException
    {
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

        return revision;
    }

    public List<String> getListing() throws SCMException
    {
        RlogCommand log = newRlogCommand();
        log.setDateFilter(SERVER_DATE.format(new Date()) + "<");

        LogDirectoryBuilder builder = new LogDirectoryBuilder();
        log.setBuilder(builder);

        getClient().executeCommand(log);

        return builder.getDirectories();
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
    private LogAnalyser getAnalyser()
    {
        if (analyser == null)
        {
            analyser = new LogAnalyser(getClient().getRoot());
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

    private VersionCommand newVersionCommand()
    {
        return new VersionCommand();
    }

}
