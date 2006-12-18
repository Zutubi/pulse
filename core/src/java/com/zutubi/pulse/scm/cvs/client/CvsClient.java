package com.zutubi.pulse.scm.cvs.client;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.core.model.CvsRevision;
import com.zutubi.pulse.scm.SCMCheckoutEventHandler;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.cvs.client.commands.LogListener;
import com.zutubi.pulse.scm.cvs.client.commands.StatusListener;
import com.zutubi.pulse.scm.cvs.client.commands.UpdateListener;
import com.zutubi.pulse.scm.cvs.client.commands.VersionCommand;
import com.zutubi.pulse.scm.cvs.client.util.CvsUtils;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.CommandAbortedException;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.command.log.RlogCommand;
import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.lib.cvsclient.command.status.StatusCommand;
import org.netbeans.lib.cvsclient.command.status.StatusInformation;
import org.netbeans.lib.cvsclient.command.tag.RtagCommand;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.connection.Connection;
import org.netbeans.lib.cvsclient.event.CVSListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

/**
 *
 */
public class CvsClient
{
    private static final SimpleDateFormat SERVER_DATE;
    static
    {
        SERVER_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        SERVER_DATE.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private CVSRoot root;
    private String password;

    public CvsClient()
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

    public String version() throws SCMException
    {
        VersionCommand version = new VersionCommand();

        if (!executeCommand(version, null, null))
        {
            throw new SCMException("failed to retrieve the cvs server version details.");
        }
        return version.getVersion();
    }

    public void update(File workingDirectory, CvsRevision revision, SCMCheckoutEventHandler handler) throws SCMException
    {
        UpdateListener listener = new UpdateListener(handler);
        update(workingDirectory, revision, listener);
    }

    public void update(File workingDirectory, CvsRevision revision, CVSListener listener) throws SCMException
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
            throw new SCMException("Failed to update.");
        }
    }

    public void checkout(File workdir, String module, CvsRevision revision, SCMCheckoutEventHandler handler) throws SCMException
    {
        checkout(workdir, module, revision, true, handler);
    }

    public void checkout(File workdir, String module, CvsRevision revision, boolean recursive, SCMCheckoutEventHandler handler) throws SCMException
    {
        CheckoutCommand checkout = new CheckoutCommand();
        checkout.setModule(module);
        checkout.setRecursive(recursive);

        if (TextUtils.stringSet(revision.getBranch()))
        {
            checkout.setCheckoutByRevision(revision.getBranch());
        }
        if (revision.getDate() != null)
        {
            checkout.setCheckoutByDate(SERVER_DATE.format(revision.getDate()));
        }

        UpdateListener listener = new UpdateListener(handler);
        if (!executeCommand(checkout, workdir, listener))
        {
            throw new SCMException("Failed to checkout.");
        }
    }

    public void tag(String module, CvsRevision revision, String name) throws SCMException
    {
        tag(module, revision, name, false);
    }
    
    public void tag(String module, CvsRevision revision, String name, boolean moveExisting) throws SCMException
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
            throw new SCMException("Failed to tag.");
        }
    }

    public void deleteTag(String module, String name) throws SCMException
    {
        RtagCommand tag = new RtagCommand();
        tag.setModules(new String[]{module});
        tag.setTag(name);
        tag.setDeleteTag(true);
        tag.setRecursive(true);
        tag.setClearFromRemoved(true);

        if (!executeCommand(tag, null, null))
        {
            throw new SCMException("Failed to delete tag.");
        }
    }

    public List<LogInformation> rlog(String module, CvsRevision from, CvsRevision to) throws SCMException
    {
        return rlog(module, from, to, false);
    }

    public List<LogInformation> rlog(String module, CvsRevision from, CvsRevision to, boolean verbose)
            throws SCMException
    {
        RlogCommand rlog = new RlogCommand();
        rlog.setModule(module);
        rlog.setSuppressHeader(!verbose);

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
            throw new SCMException("Failed to retrieve the cvs server changes between details.");
        }
        return listener.getLogInfo();
    }

    public List<StatusInformation> status(File workingCopy) throws SCMException
    {
        StatusListener listener = new StatusListener();
        status(workingCopy, null, listener);
        return listener.getInfo();
    }

    public void status(File workingCopy, File[] files, CVSListener listener) throws SCMException
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
            throw new SCMException("Failed to run status command.");
        }
    }

    public void testConnection() throws SCMException
    {
        Connection connection = null;
        try
        {
            connection = ConnectionFactory.getConnection(root, password);
            connection.verify();
        }
        catch (AuthenticationException e)
        {
            throw new SCMException(e);
        }
        finally
        {
            CvsUtils.close(connection);
        }
    }

    /**
     * Execute the cvs command.
     *
     * @param command to be executed on the configured cvs connection.
     * @param listener
     * @return true if the command is successful, false otherwise.
     *
     * @throws SCMException thrown when an error occurs.
     */
    public boolean executeCommand(Command command, File localPath, CVSListener listener) throws SCMException
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

//            LOG.info("Executing cvs command: " + command.getCVSCommand());
            long time = System.currentTimeMillis();
            try
            {
                if (!client.executeCommand(command, globalOptions))
                {
//                    LOG.warning("Cvs command: -d "+root+" '" + command.getCVSCommand() + "' has failed.");
                    return false;
                }
                return true;
            }
            finally
            {
//                LOG.info("Elapsed time: " + ((System.currentTimeMillis() - time)/ Constants.SECOND) + " second(s)");
            }
        }
        catch (AuthenticationException ae)
        {
            throw new SCMException(ae);
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
