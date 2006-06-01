package com.zutubi.pulse.scm.cvs.client;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.cvs.CvsUtils;
import com.zutubi.pulse.util.Constants;
import com.zutubi.pulse.util.logging.Logger;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.CommandAbortedException;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.lib.cvsclient.command.log.RlogCommand;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.connection.Connection;
import org.netbeans.lib.cvsclient.event.CVSListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

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

        //TODO: Integrate the following logging into the systems logging. This information
        //      will be very useful in tracking problems with the cvs client integration.
        //      It will likely require patching the cvsclient.util.Logger code.
//        org.netbeans.lib.cvsclient.util.Logger.setLogging("system");
    }

    public CVSRoot getRoot()
    {
        return root;
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

    private SCMException handleAuthenticationException(AuthenticationException ae)
    {
        return new SCMException("Authentication failure. Failed to connect to requested cvs server '" + root +
                "'. Cause: " + ae.getMessage(), ae);
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
        log.setSuppressHeader(true);

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

        executeCommand(log, new LogCommandListener(rlogResponse));

        return rlogResponse;
    }

    public boolean executeCommand(Command command) throws SCMException
    {
        return executeCommand(command, null);
    }

    /**
     * Execute the cvs command.
     *
     * @param command          to be executed on the configured cvs connection.
     * @param responseListener
     * @return true if the command is successful, false otherwise.
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

            LOG.info("Executing cvs command: " + command.getCVSCommand());
            long time = System.currentTimeMillis();
            try
            {
                if (!client.executeCommand(command, globalOptions))
                {
                    LOG.warning("Cvs command: -d "+root+" '" + command.getCVSCommand() + "' has failed.");
                    return false;
                }
                return true;
            }
            finally
            {
                LOG.info("Elapsed time: " + ((System.currentTimeMillis() - time)/ Constants.SECOND) + " second(s)");
            }
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
