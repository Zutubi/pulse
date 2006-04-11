package com.zutubi.pulse.scm.cvs;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.cvs.client.ConnectionFactory;
import com.zutubi.pulse.scm.cvs.client.CvsLogInformationListener;
import com.zutubi.pulse.scm.cvs.client.LoggingListener;
import com.zutubi.pulse.util.logging.Logger;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.CommandAbortedException;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.lib.cvsclient.command.log.RlogCommand;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.connection.Connection;
import org.netbeans.lib.cvsclient.event.CVSAdapter;
import org.netbeans.lib.cvsclient.event.CVSListener;
import org.netbeans.lib.cvsclient.event.MessageEvent;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 * Allows for the system to interact with a cvs repository.
 * <p/>
 * This class is a wrapper around the org.netbeans.lib.cvsclient package, using the
 * netbeans package to handle the cvs protocol requirements.
 */
public class CvsClient
{
    /**
     * The date format used when sending dates to the CVS server. Cvs servers talk in GMT.
     */
    private static final SimpleDateFormat CVSDATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    static
    {
        CVSDATE.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /**
     * Logging.
     */
    private static final Logger LOG = Logger.getLogger(CvsClient.class);

    /**
     * The $CVSROOT, it defines the details of which cvs repository is being worked with.
     */
    private final CVSRoot root;

    /**
     * The local path to the working repository directories. This is required for checkout / update
     * commands.
     */
    private File localPath;

    /**
     *
     */
    private String password;

    private LogAnalyser logAnalyser;

    /**
     * @param cvsRoot
     * @throws IllegalArgumentException if the cvsRoot parameter is invalid.
     */
    public CvsClient(String cvsRoot) throws IllegalArgumentException
    {
        this(CVSRoot.parse(cvsRoot));
    }

    public CvsClient(CVSRoot root)
    {
        this.root = root;
        this.logAnalyser = new LogAnalyser(root);

        //TODO: Integrate the following logging into the systems logging. This information
        //      will be very useful in tracking problems with the cvs client integration.
        //      It will likely require patching the cvsclient.util.Logger code.
        //org.netbeans.lib.cvsclient.util.Logger.setLogging("system");
    }

    /**
     * Set the path to the local copy of the repository. This is required
     * for commands that work with a local copy of the repository, such as update
     * and checkout.
     * <p/>
     * If this path does not exist, then the client will create it.
     */
    public void setLocalPath(File path)
    {
        this.localPath = path;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * Default checkout.
     *
     * @param module
     * @throws SCMException
     */
    public void checkout(String module) throws SCMException
    {
        checkout(module, null, null);
    }

    /**
     * Test the connection to the cvs server.
     *
     * @throws SCMException
     */
    public void testConnection() throws SCMException
    {
        // test connection to cvs server.
        Connection connection = null;
        try
        {
            connection = ConnectionFactory.getConnection(root, password);
            connection.verify();
        }
        catch (AuthenticationException e)
        {
            throw handleAuthenticationException(e);
        }
        finally
        {
            CvsUtils.close(connection);
        }
    }

    public String getServerVersion() throws SCMException
    {
        VersionCommand versionCommand = new VersionCommand();

        final String[] version = new String[1];
        if (!executeCommand(versionCommand, new CVSAdapter()
        {
            public void messageSent(MessageEvent e)
            {
                if (!e.isError())
                {
                    version[0] = e.getMessage();
                }
            }
        }))
        {
            throw new SCMException("failed to retrieve the cvs server version details.");
        }
        return version[0];
    }


    /**
     * Checkout the specified module, as it was on the specified date. If the date is null,
     * no date restriction will be applied.
     *
     * @param module
     * @param revision
     * @param date
     * @throws SCMException
     */
    public void checkout(String module, String revision, Date date) throws SCMException
    {
        checkout(module, revision, date, true);
    }

    public void checkout(String module, String revision, Date date, boolean recursive) throws SCMException
    {
        module = checkModule(module);

        CheckoutCommand checkout = new CheckoutCommand();
        checkout.setModule(module);
        checkout.setRecursive(recursive);

        // bind the checkout to the specified tag.
        if (revision != null)
        {
            checkout.setCheckoutByRevision(revision);
        }

        // bind the checkout to the specified date.
        if (date != null)
        {
            checkout.setCheckoutByDate(CVSDATE.format(date));
        }

        if (!executeCommand(checkout, new LoggingListener()))
        {
            throw new SCMException("Execution of checkout command failed. Reason is unknown.");
        }
    }

    private SCMException handleAuthenticationException(AuthenticationException ae)
    {
        return new SCMException("Authentication failure. Failed to connect to requested cvs server '" + root +
                "'. Cause: " + ae.getMessage(), ae);
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

        // HACK: cvs client has trouble absolute references, hanging if they are invalid.
        // Therefore, do not allow them.
        while (module.startsWith("/"))
        {
            module = module.substring(1);
        }
        return module;
    }

    /**
     * Check if the repository has been updated since the since specified. Note, the
     * updates are restricted to those that imply a change to the source. That is, commit,
     * add and remove operations.
     *
     * @param since
     * @return true if the cvs repository has been updated.
     */
    public boolean hasChangedSince(String module, String branch, Date since) throws SCMException
    {
        if (LOG.isLoggable(Level.FINER))
        {
            LOG.entering(CvsClient.class.getName(), "hasChangedSince(" + module + ", " + branch + ", " + CVSDATE.format(since) + ")");
        }
        boolean result = getLastUpdate(module, branch, since) != null;
        if (LOG.isLoggable(Level.FINER))
        {
            LOG.exiting(result);
        }
        return result;
    }

    /**
     * @param branch
     * @param since
     * @return null indicates no change since the specified date
     * @throws SCMException
     */
    public Date getLastUpdate(String module, String branch, Date since) throws SCMException
    {
        List<LogInformation> rlogResponse = rlog(module, branch, since, null, false);

        return logAnalyser.latestUpdate(rlogResponse);
    }

    /**
     * Update is not yet supported.
     */
    public void update()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Retrieve all of the change lists in the named module in the repository.
     *
     * @return
     * @throws SCMException
     */
    public List<Changelist> getChangeLists(String module) throws SCMException
    {
        return getChangeLists(module, null, null, null);
    }

    /**
     * Retrieve the list of changes in the named module since the specified date.
     *
     * @param module
     * @param branch
     * @param from
     * @return
     * @throws SCMException
     */
    public List<Changelist> getChangeLists(String module, String branch, Date from, Date to) throws SCMException
    {
        // retrieve the log info for all of the files that have been modified.
        List<LogInformation> rlogResponse = rlog(module, branch, from, to, false);
        
        return logAnalyser.extract(rlogResponse);
    }

    /**
     * This rlog command returns a list of Revision instances that define the
     * individual files and there revisions that were generated from the specified date in the
     * named module. These revisions are ordered chronologically.
     *
     * @param module
     * @param branch
     * @param from
     * @param headersOnly
     * @return
     * @throws SCMException
     */
    public List<LogInformation> rlog(String module, String branch, Date from, Date to, boolean headersOnly) throws SCMException
    {
        final List<LogInformation> rlogResponse = new LinkedList<LogInformation>();

        RlogCommand log = new RlogCommand();
        log.setModule(module);

//        log.setHeaderOnly(headersOnly);
//        log.setNoTags(headersOnly);

        String dateFilter = "";
        String del = "<=";
        if (from != null)
        {
            dateFilter = CVSDATE.format(from) + del;
            del = "";
        }
        if (to != null)
        {
            dateFilter += del + CVSDATE.format(to);
        }
        if (TextUtils.stringSet(dateFilter))
        {
            log.setDateFilter(dateFilter);
        }

        if (TextUtils.stringSet(branch))
        {
            // branch..
            log.setRevisionFilter(branch);
        }
        else
        {
            log.setDefaultBranch(true); // work with head.
        }

        executeCommand(log, new CvsLogInformationListener(rlogResponse));

        return rlogResponse;
    }

    /**
     * Execute the cvs command.
     *
     * @param command to be executed on the configured cvs connection.
     * @param responseListener
     *
     * @return true if the command is successful, false otherwise.
     *
     * @throws SCMException
     */
    public boolean executeCommand(Command command, CVSListener responseListener) throws SCMException
    {
        Connection connection = null;
        try
        {
            GlobalOptions globalOptions = new GlobalOptions();
            globalOptions.setCVSRoot(root.toString());

            connection = openConnection();

            Client client = new Client(connection, new StandardAdminHandler());
            if (responseListener != null)
            {
                client.getEventManager().addCVSListener(responseListener);
            }
            if (localPath != null)
            {
                client.setLocalPath(localPath.getAbsolutePath());
            }

            return client.executeCommand(command, globalOptions);
        }
        catch (AuthenticationException ae)
        {
            throw handleAuthenticationException(ae);
        }
        catch (CommandAbortedException cae)
        {
            throw new SCMException(cae);
        }
        catch (CommandException ce)
        {
            throw new SCMException(ce);
        }
        finally
        {
            CvsUtils.close(connection);
        }
    }

    private Connection openConnection() throws AuthenticationException, CommandAbortedException
    {
        Connection connection = ConnectionFactory.getConnection(root, password);
        connection.open();
        return connection;
    }
}
