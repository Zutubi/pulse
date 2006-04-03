package com.cinnamonbob.scm.cvs;

import com.cinnamonbob.core.model.Change;
import com.cinnamonbob.core.model.Change.Action;
import com.cinnamonbob.core.model.Changelist;
import com.cinnamonbob.core.model.CvsRevision;
import com.cinnamonbob.scm.SCMException;
import com.cinnamonbob.scm.cvs.client.ConnectionFactory;
import com.cinnamonbob.scm.cvs.client.CvsLogInformationListener;
import com.cinnamonbob.scm.cvs.client.LoggingListener;
import com.cinnamonbob.util.logging.Logger;
import com.opensymphony.util.TextUtils;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.command.CommandAbortedException;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.lib.cvsclient.command.log.RlogCommand;
import org.netbeans.lib.cvsclient.command.tag.RtagCommand;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.connection.Connection;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
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
     * The date format used when sending dates to the CVS server.
     */
    private static final SimpleDateFormat CVSDATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    static {
        // cvs servers talk in GMT.
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
     *
     * @throws SCMException
     */
    public void checkout(String module) throws SCMException
    {
        checkout(module, null, null);
    }

    public void testConnection(String module) throws SCMException
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

        // test defined module.

    }

    /**
     * Checkout the specified module, as it was on the specified date. If the date is null,
     * no date restriction will be applied.
     *
     * @param module
     * @param revision
     * @param date
     *
     * @throws SCMException
     */
    public void checkout(String module, String revision, Date date) throws SCMException
    {
        module = checkModule(module);

        Connection connection = null;
        try
        {
            GlobalOptions globalOptions = new GlobalOptions();
            globalOptions.setCVSRoot(root.toString());

            connection = ConnectionFactory.getConnection(root, password);
            connection.open();

            Client client = new Client(connection, new StandardAdminHandler());
            client.getEventManager().addCVSListener(new LoggingListener());
            client.setLocalPath(localPath.getAbsolutePath());

            CheckoutCommand checkout = new CheckoutCommand();
            checkout.setModule(module);
            checkout.setPruneDirectories(true);

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

            if (!client.executeCommand(checkout, globalOptions))
            {
                LOG.error("Execution of checkout command failed. Reason is unknown.");
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
            LOG.entering(CvsClient.class.getName(), "hasChangedSince("+module+", "+branch+", "+CVSDATE.format(since)+")");
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
        List<LocalChange> changes = getLocalChanges(module, branch, since, null);
        if (changes.size() == 0)
        {
            return null;
        }
        // need to ensure that the log information is ordered by date...
        LocalChange latestChange = changes.get(changes.size() - 1);
        return latestChange.getDate();
    }

    /**
     * Update is not yet supported.
     */
    public void update()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Tag the remote repository.
     *
     * @param tag
     * @param module
     * @param date
     * @throws SCMException
     */
    public void tag(String tag, String module, Date date) throws SCMException
    {
        // WARNING: This has not been tested...
        Connection connection = null;
        try
        {
            GlobalOptions globalOptions = new GlobalOptions();
            globalOptions.setCVSRoot(root.toString());

            connection = ConnectionFactory.getConnection(root, password);
            connection.open();

            Client client = new Client(connection, new StandardAdminHandler());

            RtagCommand rtag = new RtagCommand();
            rtag.setTag(tag);
            rtag.setOverrideExistingTag(true);
            rtag.setModules(new String[]{module});
            if (date != null)
            {
                rtag.setTagByDate(CVSDATE.format(date));
            }
            client.executeCommand(rtag, globalOptions);
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

    //-------------------------------------------------------------------------
    // change set analysis:
    // - cvs changes are not atomic. therefore,
    //    - a change set does not need to occur at the same time
    //    - multiple changesets can be interlevered.
    // characteristics of changesets:
    // - a) single author.
    // - b) single commit statement.
    // - c) each file appears only once.
    // - d) changeset bound to a single branch.
    // - e) contiguous block of time.

    // group by (author,branch,comment)

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
        List<LocalChange> simpleChanges = getLocalChanges(module, branch, from, to);

        // group by author, branch, sort by date. this will have the affect of grouping
        // all of the changes in a single changeset together, ordered by date.
        Collections.sort(simpleChanges, new Comparator<LocalChange>()
        {
            public int compare(LocalChange changeA, LocalChange changeB)
            {
                int comparison = changeA.getAuthor().compareTo(changeB.getAuthor());
                if (comparison != 0)
                {
                    return comparison;
                }
                // tags should never be different.
                comparison = changeA.getTag().compareTo(changeB.getTag());
                if (comparison != 0)
                {
                    return comparison;
                }
                return changeA.getDate().compareTo(changeB.getDate());
            }
        });

        // create change sets by author. ie: each change set object will contain
        // all of the changes made by a particular author.
        List<LocalChangeSet> changeSets = new LinkedList<LocalChangeSet>();
        LocalChangeSet changeSet = null;
        for (LocalChange change : simpleChanges)
        {
            if (changeSet == null)
            {
                changeSet = new LocalChangeSet(change);
            }
            else
            {
                if (changeSet.belongsTo(change))
                {
                    changeSet.add(change);
                }
                else
                {
                    changeSets.add(changeSet);
                    changeSet = new LocalChangeSet(change);
                }
            }
        }
        if (changeSet != null)
        {
            changeSets.add(changeSet);
        }

        // refine the changesets, splitting it up according to file names. ie: duplicate filenames
        // should trigger a new changeset.
        List<LocalChangeSet> refinedSets = new LinkedList<LocalChangeSet>();
        for (LocalChangeSet set : changeSets)
        {
            refinedSets.addAll(set.refine());
        }

        // now that we have the changeset information, lets create the final product.
        List<Changelist> changelists = new LinkedList<Changelist>();
        for (LocalChangeSet set : refinedSets)
        {
            List<LocalChange> localChanges = set.getChanges();
            // we use the last change because it has the most recent date. all the other information is
            // is common to all the changes.
            LocalChange lastChange = localChanges.get(localChanges.size() - 1);
            CvsRevision rev = new CvsRevision(lastChange.getAuthor(), lastChange.getTag(), lastChange.getMessage(), lastChange.getDate());
            Changelist changelist = new Changelist(rev);
            for (LocalChange change : localChanges)
            {
                changelist.addChange(new Change(change.getFilename(), change.getRevision(), change.getAction()));
            }
            changelists.add(changelist);
        }

        return changelists;
    }

    private List<LocalChange> getLocalChanges(String module, String branch, Date from, Date to) throws SCMException
    {
        List<LogInformation> rlogResponse = rlog(module, branch, from, to, false);

        // extract the returned revisions
        List<LocalChange> revisions = new LinkedList<LocalChange>();
        for (LogInformation logInfo : rlogResponse)
        {
            for (Object obj : logInfo.getRevisionList())
            {
                LogInformation.Revision rev = (LogInformation.Revision) obj;
                LocalChange change = new LocalChange(rev);
                change.setTag(branch);
                revisions.add(change);
            }
        }
        // and order them chronologically.
        Collections.sort(revisions, new Comparator<LocalChange>()
        {
            public int compare(LocalChange o1, LocalChange o2)
            {
                return o1.getDate().compareTo(o2.getDate());
            }
        });
        return revisions;

    }

    /**
     * This rlog command returns a list of LocalChange instances that define the
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
        Connection connection = null;
        try
        {
            GlobalOptions globalOptions = new GlobalOptions();
            globalOptions.setCVSRoot(root.toString());

            connection = ConnectionFactory.getConnection(root, password);
            connection.open();

            final List<LogInformation> rlogResponse = new LinkedList<LogInformation>();

            // the local path is not important for the RLogCommand, but needs to exist... go figure..
            // should we try setting the real local path if it exists?
            Client client = new Client(connection, new StandardAdminHandler());
            client.setLocalPath("/some/made/up/path");
            client.getEventManager().addCVSListener(new CvsLogInformationListener(rlogResponse));

            RlogCommand log = new RlogCommand();
            log.setModule(module);
            log.setHeaderOnly(headersOnly);
            log.setNoTags(headersOnly);

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

            client.executeCommand(log, globalOptions);

            return rlogResponse;
        }
        catch (AuthenticationException ae)
        {
            throw handleAuthenticationException(ae);
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

    /**
     * Simple value object used to help store data during the changeset analysis process.
     */
    public class LocalChange
    {
        private LogInformation.Revision log;

        private String tag;

        public LocalChange(LogInformation.Revision log)
        {
            if (log == null)
            {
                throw new IllegalArgumentException("Log Information cannot be null.");
            }
            this.log = log;
        }

        public String getAuthor()
        {
            return log.getAuthor();
        }

        public String getRevision()
        {
            return log.getNumber();
        }

        public String getTag()
        {
            if (tag == null)
            {
                return "";
            }
            return tag;
        }

        public void setTag(String branch)
        {
            this.tag = branch;
        }

        public Date getDate()
        {
            return log.getDate();
        }

        public String getMessage()
        {
            return log.getMessage();
        }

        public String getFilename()
        {
            // need to process the filename.

            String filename = log.getLogInfoHeader().getRepositoryFilename();

            // remove the ,v
            if (filename.endsWith(",v"))
                filename = filename.substring(0, filename.length() - 2);

            // remove the repo root.
            if (filename.startsWith(root.getRepository()))
                filename = filename.substring(root.getRepository().length());

            return filename;
        }

        public Action getAction()
        {
            if (log.getAddedLines() == 0 && log.getRemovedLines() == 0)
            {
                if (!log.getState().equalsIgnoreCase("dead"))
                {
                    return Action.ADD;
                }
                return Action.DELETE;
            }
            return Action.EDIT;
        }

    }

    /**
     * Simple value object used to help store data during the changeset analysis process.
     */
    private class LocalChangeSet
    {
        private final List<LocalChange> changes = new LinkedList<LocalChange>();

        LocalChangeSet(LocalChange c)
        {
            changes.add(c);
        }

        void add(LocalChange c)
        {
            changes.add(c);
        }

        boolean belongsTo(LocalChange otherChange)
        {
            if (changes.size() == 0)
            {
                return true;
            }

            LocalChange previousChange = changes.get(0);
            return previousChange.getAuthor().equals(otherChange.getAuthor()) &&
                    previousChange.getTag().equals(otherChange.getTag()) &&
                    previousChange.getMessage().equals(otherChange.getMessage());
        }

        /**
         *
         */
        public List<LocalChangeSet> refine()
        {
            Map<String, String> filenames = new HashMap<String, String>();
            List<LocalChangeSet> changesets = new LinkedList<LocalChangeSet>();

            LocalChangeSet changeSet = null;
            for (LocalChange change : changes)
            {
                if (filenames.containsKey(change.getFilename()))
                {
                    // time for a new changeset.
                    filenames.clear();
                    changesets.add(changeSet);
                    filenames.put(change.getFilename(), change.getFilename());
                    changeSet = new LocalChangeSet(change);
                }
                else
                {
                    filenames.put(change.getFilename(), change.getFilename());
                    if (changeSet == null)
                    {
                        changeSet = new LocalChangeSet(change);
                    }
                    else
                    {
                        changeSet.add(change);
                    }
                }
            }
            if (changeSet != null)
            {
                changesets.add(changeSet);
            }
            return changesets;
        }

        public List<LocalChange> getChanges()
        {
            return changes;
        }

    }

}
