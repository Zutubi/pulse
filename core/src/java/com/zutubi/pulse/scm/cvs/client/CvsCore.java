package com.zutubi.pulse.scm.cvs.client;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.scm.cvs.CvsRevision;
import com.zutubi.pulse.scm.ScmEventHandler;
import com.zutubi.pulse.scm.ScmException;
import com.zutubi.pulse.scm.cvs.client.commands.*;
import com.zutubi.pulse.scm.cvs.client.util.CvsUtils;
import com.zutubi.util.Constants;
import com.zutubi.util.logging.Logger;
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
import org.netbeans.lib.cvsclient.command.status.StatusCommand;
import org.netbeans.lib.cvsclient.command.status.StatusInformation;
import org.netbeans.lib.cvsclient.command.tag.RtagCommand;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.connection.Connection;
import org.netbeans.lib.cvsclient.event.CVSAdapter;
import org.netbeans.lib.cvsclient.event.CVSListener;
import org.netbeans.lib.cvsclient.event.MessageEvent;

import java.io.File;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 *
 */
public class CvsCore
{
    public static final Logger LOG = Logger.getLogger(CvsCore.class);

    private static int COMMAND_COUNT = 0;

    private static final SimpleDateFormat SERVER_DATE;
    static
    {
        SERVER_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        SERVER_DATE.setTimeZone(TimeZone.getTimeZone("GMT"));

        try
        {
            Field outLogStream = org.netbeans.lib.cvsclient.util.Logger.class.getDeclaredField("outLogStream");
            Field inLogStream = org.netbeans.lib.cvsclient.util.Logger.class.getDeclaredField("inLogStream");
            Field logging = org.netbeans.lib.cvsclient.util.Logger.class.getDeclaredField("logging");

            outLogStream.setAccessible(true);
            inLogStream.setAccessible(true);
            logging.setAccessible(true);

            outLogStream.set(org.netbeans.lib.cvsclient.util.Logger.class, new LoggingOutputStream(LOG, Level.FINER));
            inLogStream.set(org.netbeans.lib.cvsclient.util.Logger.class, new LoggingOutputStream(LOG, Level.FINEST));
            logging.set(org.netbeans.lib.cvsclient.util.Logger.class, Boolean.TRUE);
        }
        catch (Exception e)
        {
            LOG.warning(e);
        }
    }

    private CVSRoot root;
    private String password;
    private static final String RLOG_SUPPRESS_HEADER = "cvs.rlog.suppressHeader";

    public CvsCore()
    {
    }

    public void setRoot(CVSRoot root)
    {
        this.root = root;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String version() throws ScmException
    {
        VersionCommand version = new VersionCommand();

        if (!executeCommand(version, null, null))
        {
            throw new ScmException("failed to retrieve the cvs server version details.");
        }
        return version.getVersion();
    }

    public void update(File workingDirectory, CvsRevision revision, ScmEventHandler handler) throws ScmException
    {
        UpdateListener listener = null;
        if (handler != null)
        {
            listener = new UpdateListener(handler);
        }
        update(workingDirectory, revision, listener);
    }

    public void update(File workingDirectory, CvsRevision revision, CVSListener listener) throws ScmException
    {
        UpdateCommand update = new UpdateCommand();
        update.setPruneDirectories(true);
        update.setBuildDirectories(true);
        update.setResetStickyOnes(true);
        if (revision != null)
        {
            if (TextUtils.stringSet(revision.getBranch()))
            {
                update.setUpdateByRevision(revision.getBranch());
            }
            if (revision.getDate() != null)
            {
                update.setUpdateByDate(SERVER_DATE.format(revision.getDate()));
            }
        }

        if (!executeCommand(update, workingDirectory, listener))
        {
            throw new ScmException("Failed to update.");
        }
    }

    public void checkout(File workdir, String module, CvsRevision revision, ScmEventHandler handler) throws ScmException
    {
        checkout(workdir, module, revision, true, handler);
    }

    public void checkout(File workdir, String module, CvsRevision revision, boolean recursive, ScmEventHandler handler) throws ScmException
    {
        CheckoutCommand checkout = new CheckoutCommand();
        checkout.setModule(module);
        checkout.setRecursive(recursive);

        if (revision != null)
        {
            if (TextUtils.stringSet(revision.getBranch()))
            {
                checkout.setCheckoutByRevision(revision.getBranch());
            }
            if (revision.getDate() != null)
            {
                checkout.setCheckoutByDate(SERVER_DATE.format(revision.getDate()));
            }
        }

        CheckoutListener listener = null;
        if (handler != null)
        {
            listener = new CheckoutListener(handler);
        }
        
        if (!executeCommand(checkout, workdir, listener))
        {
            throw new ScmException("Failed to checkout.");
        }
    }

    public void tag(String module, CvsRevision revision, String name) throws ScmException
    {
        tag(module, revision, name, false);
    }
    
    public void tag(String module, CvsRevision revision, String name, boolean moveExisting) throws ScmException
    {
        RtagCommand tag = new RtagCommand();
        tag.setModules(new String[]{module});
        tag.setTag(name);
        tag.setOverrideExistingTag(moveExisting);
        tag.setRecursive(true);

        if (TextUtils.stringSet(revision.getBranch()))
        {
            tag.setTagByRevision(revision.getBranch());
        }
        if (revision.getDate() != null)
        {
            tag.setTagByDate(SERVER_DATE.format(revision.getDate()));
        }

        if (!executeCommand(tag, null, null))
        {
            throw new ScmException("Failed to tag.");
        }
    }

    public void deleteTag(String module, String name) throws ScmException
    {
        RtagCommand tag = new RtagCommand();
        tag.setModules(new String[]{module});
        tag.setTag(name);
        tag.setDeleteTag(true);
        tag.setRecursive(true);
        tag.setClearFromRemoved(true);

        if (!executeCommand(tag, null, null))
        {
            throw new ScmException("Failed to delete tag.");
        }
    }

    public List<LogInformation> rlog(String module, CvsRevision from, CvsRevision to) throws ScmException
    {
        return rlog(module, from, to, false);
    }

    public List<LogInformation> rlog(String module, CvsRevision from, CvsRevision to, boolean verbose)
            throws ScmException
    {
        RlogCommand rlog = new RlogCommand();
        rlog.setModule(module);

        // allow users to bypass 
        boolean useSuppressHeader = true;
        if (System.getProperties().containsKey(RLOG_SUPPRESS_HEADER))
        {
            useSuppressHeader = Boolean.getBoolean(RLOG_SUPPRESS_HEADER);
        }
        if (useSuppressHeader && !verbose)
        {
            rlog.setSuppressHeader(true);
        }

        String branch = from == null ? to == null ? null : to.getBranch() : from.getBranch();
        if (TextUtils.stringSet(branch))
        {
            rlog.setRevisionFilter(branch);
        }
        else
        {
            rlog.setDefaultBranch(true);
        }
        String dateFilter = "";
        String del = "<=";
        if (from != null && from.getDate() != null)
        {
            dateFilter = (new StringBuilder()).append(SERVER_DATE.format(from.getDate())).append(del).toString();
            del = "";
        }
        if (to != null && to.getDate() != null)
        {
            dateFilter = (new StringBuilder()).append(dateFilter).append(del).append(SERVER_DATE.format(to.getDate())).toString();
        }
        if (TextUtils.stringSet(dateFilter))
        {
            rlog.setDateFilter(dateFilter);
        }
        
        LogListener listener = new LogListener();
        if (!executeCommand(rlog, null, listener))
        {
            throw new ScmException("Failed to retrieve the cvs server changes between revisions.");
        }
        return listener.getLogInfo();
    }

    public List<StatusInformation> status(File workingCopy) throws ScmException
    {
        StatusListener listener = new StatusListener();
        status(workingCopy, null, listener);
        return listener.getInfo();
    }

    public void status(File workingCopy, File[] files, CVSListener listener) throws ScmException
    {
        StatusCommand status = new StatusCommand();
        status.setRecursive(true);
        if(files == null)
        {
            status.setFiles(new File[]{workingCopy});
        }
        else
        {
            status.setFiles(files);
        }

        if (!executeCommand(status, workingCopy, listener))
        {
            throw new ScmException("Failed to run status command.");
        }
    }

    public void testConnection() throws ScmException
    {
        Connection connection = null;
        try
        {
            connection = ConnectionFactory.getConnection(root, password);
            connection.verify();
        }
        catch (AuthenticationException e)
        {
            throw new ScmException(e);
        }
        finally
        {
            CvsUtils.close(connection);
        }
    }

    /**
     * Execute the cvs command.
     *
     * @param command   to be executed on the configured cvs connection.
     * @param localPath local path which the command refers to (may be null)
     * @param listener  receives feedback from executing the command (may be
     *                  null)
     * @return true if the command is successful, false otherwise.
     *
     * @throws ScmException thrown when an error occurs.
     */
    public boolean executeCommand(Command command, File localPath, CVSListener listener) throws ScmException
    {
        Connection connection = null;
        try
        {
            GlobalOptions globalOptions = new GlobalOptions();
            globalOptions.setCVSRoot(root.toString());

            connection = openConnection();

            Client client = new Client(connection, new StandardAdminHandler());
            if (listener != null)
            {
                client.getEventManager().addCVSListener(listener);
            }
            if (localPath != null)
            {
                client.setLocalPath(localPath.getAbsolutePath());
            }

            client.getEventManager().addCVSListener(new CVSAdapter()
            {
                public void messageSent(MessageEvent e)
                {
                    LOG.finer(e.getMessage() + "\n");
                }
            });

            long time = System.currentTimeMillis();
            try
            {
                CvsDebugFormatter.contextHolder.set("" + (++COMMAND_COUNT)); // include the id of the scm?..
                LOG.info("Executing command: 'cvs -d "+root+" " + command.getCVSCommand() + "'.");
                if (!client.executeCommand(command, globalOptions))
                {
                    LOG.warning("Command 'cvs -d "+root+" " + command.getCVSCommand() + "' has failed.");
                    return false;
                }
                return true;
            }
            finally
            {
                LOG.finer("Elapsed time: " + ((System.currentTimeMillis() - time)/ Constants.SECOND) + " second(s)");
                CvsDebugFormatter.contextHolder.set(null);
            }
        }
        catch (AuthenticationException ae)
        {
            throw new ScmException(ae);
        }
        catch (CommandAbortedException cae)
        {
            throw new ScmException(cae);
        }
        catch (CommandException ce)
        {
            throw new ScmException(ce);
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
