package com.cinnamonbob.scm.cvs;

import com.cinnamonbob.scm.SCMException;
import com.cinnamonbob.scm.cvs.client.*;
import com.cinnamonbob.model.Changelist;
import com.cinnamonbob.model.CvsRevision;
import com.cinnamonbob.model.Change;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.command.CommandAbortedException;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.lib.cvsclient.command.log.RlogCommand;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.command.history.HistoryCommand;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.connection.Connection;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Allows for the system to interact with a cvs repository.
 * <p/>
 * This class is a wrapper around the org.netbeans.lib.cvsclient package, using the
 * netbeans package to handle the cvs protocol requirements.
 */
public class CvsClient
{
    private static final DateFormat CVS_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private CVSRoot root;

    private File localPath;

    private String branch;

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

    /**
     * Set the working branch.
     *
     * @param branch
     */
    public void setBranch(String branch)
    {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    //NOTE: at the moment, this method simply checks out the head revision of
    //      the cvs repository. Branch checkout and specific revision checkout
    //      is still to be implemented.
    public void checkout(String module) throws SCMException
    {
        Connection connection = null;
        try
        {
            GlobalOptions globalOptions = new GlobalOptions();
            globalOptions.setCVSRoot(root.toString());

            connection = ConnectionFactory.getConnection(root);
            connection.open();

            Client client = new Client(connection, new StandardAdminHandler());
            client.setLocalPath(localPath.getAbsolutePath());
            client.getEventManager().addCVSListener(new LoggingListener());

            CheckoutCommand checkout = new CheckoutCommand();
            checkout.setModule(module);

            if (!client.executeCommand(checkout, globalOptions))
            {
                throw new SCMException("checkout failed..");
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
            // cleanup any resources used by this command.
            CvsHelper.close(connection);
        }

    }

    /**
     * Check if the repository has been updated since the since specified. Note, the
     * updates are restricted to those that imply a change to the source. That is, commit,
     * add and remove operations.
     *
     * @param since
     * @return true if the cvs repository has been updated.
     */
    public boolean hasChangedSince(Date since) throws SCMException
    {
        return getLastUpdate(since) != null;
    }

    /**
     *
     * @param since
     * @return null indicates no change since the specified date
     * @throws SCMException
     */
    public Date getLastUpdate(Date since) throws SCMException
    {
        List<HistoryInfo> changes = retrieveHistoryInformation(since);
        if (changes.size() == 0)
        {
            return null;
        }
        HistoryInfo latestChange = changes.get(changes.size() - 1);
        return latestChange.getInfoDate();
    }

    public void update()
    {
        throw new UnsupportedOperationException();
    }

    public void tag()
    {
        throw new UnsupportedOperationException();
    }

    private List<HistoryInfo> retrieveHistoryInformation(Date since) throws SCMException
    {
        Connection connection = null;
        try
        {
            GlobalOptions globalOptions = new GlobalOptions();
            globalOptions.setCVSRoot(root.toString());

            connection = ConnectionFactory.getConnection(root);
            connection.open();

            Client client = new Client(connection, new StandardAdminHandler());

            HistoryBuilder builder = new HistoryBuilder();
            client.getEventManager().addCVSListener(new BuilderAdapter(builder));

            HistoryCommand history = new HistoryCommand();
            history.setReportCommits(true);
            history.setForAllUsers(true);
            if (since != null)
            {
                history.setSinceDate(CVS_DATE_FORMAT.format(since));
            }
            client.executeCommand(history, globalOptions);

            return builder.getHistoryInfo();
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
            CvsHelper.close(connection);
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

    public List<Changelist> getChangeLists(Date since) throws SCMException
    {
        List<HistoryInfo> infos = retrieveHistoryInformation(since);

        // we are only interested in commits.
        Iterator<HistoryInfo> i = infos.iterator();
        while (i.hasNext())
        {
            HistoryInfo info = i.next();
            if (!info.isCommit())
            {
                i.remove();
            }
        }

        // what file separator is the remote system using? we want to use the same.
        String fileSeparator = null;
        for (HistoryInfo info : infos)
        {
            String pathInRepository = info.getPathInRepository();
            if (pathInRepository.indexOf('\\') != -1)
            {
                fileSeparator = "\\";
                break;
            } else if (pathInRepository.indexOf('/') != -1)
            {
                fileSeparator = "/";
                break;
            }
        }

        // extract the files that have been modified from the history info, and
        // request full log info so that we can construct the changeset.
        Set<String> modifiedFiles = new HashSet<String>();
        for (HistoryInfo info : infos)
        {
            String filePath = info.getPathInRepository() + fileSeparator + info.getFile();
            modifiedFiles.add(filePath);
        }

        // retrieve the log info for all of the files that have been modified.
        Map<String, LogInformation> logInfos = retrieveLogInformation(new LinkedList<String>(modifiedFiles));

        // extract the individual changes associated with the history data and the associated
        // information.
        List<LocalChange> simpleChanges = new LinkedList<LocalChange>();
        for (HistoryInfo histInfo : infos)
        {
            String filename = histInfo.getFile();
            //String revision = histInfo.getRevision();
            String pathInRepo = histInfo.getPathInRepository();

            String fullPath = pathInRepo + fileSeparator + filename;

            // find the firstRevision -> comment, author
            LogInformation logInfo = logInfos.get(fullPath);
            assert(logInfo != null);
            //LogInformation.Revision rev = logInfo.getRevision(revision);

            //CvsRevision cvsRev = new CvsRevision(rev.getAuthor(), logInfo.getBranch(), rev.getMessage(), rev.getDate());
            LocalChange change = new LocalChange(logInfo, histInfo);//new LocalChange(fullPath, cvsRev.toString(), histInfo.getAction());
            simpleChanges.add(change);
        }

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
                comparison = changeA.getBranch().compareTo(changeB.getBranch());
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
                } else
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
            LocalChange aChange = set.getChanges().get(0);
            CvsRevision revision = new CvsRevision(aChange.getAuthor(), aChange.getBranch(), aChange.getMessage(), aChange.getDate());
            Changelist changelist = new Changelist(revision);
            for (LocalChange change : set.getChanges())
            {
                changelist.addChange(new Change(change.getFilename(), change.getRevision(), change.getAction()));
            }
            changelists.add(changelist);
        }

        return changelists;
    }

    private Map<String, LogInformation> retrieveLogInformation(List<String> modifiedFiles) throws SCMException
    {
        Connection connection = null;
        try
        {
            GlobalOptions globalOptions = new GlobalOptions();
            globalOptions.setCVSRoot(root.toString());

            connection = ConnectionFactory.getConnection(root);
            connection.open();

            final Map<String, LogInformation> infos = new HashMap<String, LogInformation>();
            final List<LogInformation> rlogResponse = new LinkedList<LogInformation>();

            Client client = new Client(connection, new StandardAdminHandler());

            // the local path is not important for the RLogCommand, but needs to exist... go figure..
            // should we try setting the real local path if it exists?
            client.setLocalPath("/some/local/path");

            client.getEventManager().addCVSListener(new CvsLogInformationListener(rlogResponse));

            RlogCommand log = new RlogCommand();
            for (String f : modifiedFiles)
            {
                log.setModule(f);
            }

            client.executeCommand(log, globalOptions);

            // match up the log info with the modified file names.
            assert(rlogResponse.size() == modifiedFiles.size());

            // the response from the rlog command should be in the same order as
            // our request, which is defined by the modified files list.
            for (int i = 0; i < rlogResponse.size(); i++)
            {
                String modifiedFilename = modifiedFiles.get(i);
                LogInformation logInfo = rlogResponse.get(i);
                assert(logInfo.getRepositoryFilename().endsWith(modifiedFilename + ",v"));
                infos.put(modifiedFilename, logInfo);
            }
            return infos;
        }
        catch (AuthenticationException ae)
        {
            throw new SCMException(ae);
        }
        catch (CommandException ce)
        {
            throw new SCMException(ce);
        }
        finally
        {
            // cleanup any resources used by this command.
            CvsHelper.close(connection);
        }
    }

    /**
     * Simple value object used to help store data during the changeset analysis process.
     *
     */
    private class LocalChange
    {
        private LogInformation log;
        private HistoryInfo history;

        public LocalChange(LogInformation log, HistoryInfo history)
        {
            this.log = log;
            this.history = history;
        }

        public String getAuthor()
        {
            return log.getRevision(getRevision()).getAuthor();
        }

        public String getRevision()
        {
            return history.getRevision();
        }

        public String getBranch()
        {
            if (log.getBranch() == null)
            {
                return "";
            }
            return log.getBranch();
        }

        public Date getDate()
        {
            return log.getRevision(getRevision()).getDate();
        }

        public String getMessage()
        {
            return log.getRevision(getRevision()).getMessage();
        }

        public String getFilename()
        {
            return log.getRepositoryFilename();
        }

        public Change.Action getAction()
        {
            return history.getAction();
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
                    previousChange.getBranch().equals(otherChange.getBranch()) &&
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
