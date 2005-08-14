package com.cinnamonbob.scm.cvs;

import com.cinnamonbob.scm.*;
import com.cinnamonbob.scm.cvs.client.BuilderAdapter;
import com.cinnamonbob.scm.cvs.client.ConnectionFactory;
import com.cinnamonbob.scm.cvs.client.HistoryBuilder;
import com.cinnamonbob.scm.cvs.client.HistoryInformation;
import com.cinnamonbob.model.SimpleChange;
import com.cinnamonbob.model.SimpleChangelist;
import com.cinnamonbob.model.CvsRevision;
import com.cinnamonbob.model.Revision;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.command.CommandAbortedException;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.command.history.HistoryCommand;
import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.lib.cvsclient.command.log.RlogCommand;
import org.netbeans.lib.cvsclient.commandLine.BasicListener;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.connection.Connection;
import org.netbeans.lib.cvsclient.event.CVSAdapter;
import org.netbeans.lib.cvsclient.event.FileInfoEvent;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 
 *
 */
public class CVSServer
{
    private CVSRoot cvsRoot;

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public CVSServer(String root)
    {
        this(CVSRoot.parse(root));
    }

    public CVSServer(CVSRoot root)
    {
        cvsRoot = root;
    }

    /**
     * Execute a cvs checkout.
     *
     * @param toDirectory is the directory into which the the module is checked out.
     * @param module is the name of the cvs module to be checked out.
     *
     * @throws SCMException
     */
    public void checkout(File toDirectory, String module) throws SCMException
    {
        // TODO: support working with branches.
        String branch = null;

        Connection connection = null;
        try
        {
            GlobalOptions globalOptions = new GlobalOptions();
            globalOptions.setCVSRoot(cvsRoot.toString());

            connection = ConnectionFactory.getConnection(cvsRoot);
            connection.open();

            Client client = new Client(connection, new StandardAdminHandler());
            client.setLocalPath(toDirectory.getAbsolutePath());

            //TODO: redirect output from basic listener to somewhere appropriate.
            client.getEventManager().addCVSListener(new BasicListener());

            CheckoutCommand checkout = new CheckoutCommand();
            checkout.setModule(module);

            if (branch != null)
            {
                checkout.setCheckoutByRevision(branch);
            }

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
            CVSHelper.close(connection);
        }
    }


    /**
     * @see SCMServer#getChanges(Revision, Revision, String...)
     */
    public List<Changelist> getChanges(Revision from, Revision to, String ...paths)
            throws SCMException
    {
        return getChanges((CvsRevision) from, (CvsRevision) to, paths);
    }

    public List<Changelist> getChanges(CvsRevision from, CvsRevision to, String ...paths)
            throws SCMException
    {

        from.getDate();
        to.getDate();
        return null;
    }

    public List<Changelist> getChanges(File toDirectory, Date since) throws SCMException
    {

        List<HistoryInformation> h = getHistoryInfo(since);

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

        // what file separator is the remote system using? we want to use the same.
        String fileSeparator = null;
        for (HistoryInformation info : h)
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

        for (HistoryInformation info : h)
        {
            if (info.isCommit())
            {
                String filePath = info.getPathInRepository() + fileSeparator + info.getFile();
                modifiedFiles.add(filePath);
            }
        }

        Map<String, LogInformation> modifiedFileInfo = getLogInfo(new LinkedList<String>(modifiedFiles));

        // extract the revisions associated with the history data.
        List<SimpleChange> simpleChanges = new LinkedList<SimpleChange>();
        for (HistoryInformation info : h)
        {
            if (!info.isCommit())
            {
                continue;
            }

            String filename = info.getFile();
            String revision = info.getRevision();
            String pathInRepo = info.getPathInRepository();

            String fullPath = pathInRepo + fileSeparator + filename;

            // find the firstRevision -> comment, author
            LogInformation logInfo = modifiedFileInfo.get(fullPath);
            assert(logInfo != null);
            LogInformation.Revision rev = logInfo.getRevision(revision);

            CvsRevision cvsrevision = new CvsRevision(rev.getAuthor(), logInfo.getBranch(), rev.getMessage(), rev.getDate());
            SimpleChange change = new SimpleChange(fullPath, cvsrevision.toString(), info.getAction());
            simpleChanges.add(change);
        }

        // group by author, branch, sort by date. this will have the affect of grouping
        // all of the changes in a single changeset together, ordered by date.
        Collections.sort(simpleChanges, new Comparator<SimpleChange>()
        {
            public int compare(SimpleChange changeA, SimpleChange changeB)
            {
                CvsRevision revisionA = null;//(CvsRevision) changeA.getRevision();
                CvsRevision revisionB = null;//(CvsRevision) changeB.getRevision();

                int comparison = revisionA.getAuthor().compareTo(revisionB.getAuthor());
                if (comparison != 0)
                {
                    return comparison;
                }
                comparison = revisionA.getBranch().compareTo(revisionB.getBranch());
                if (comparison != 0)
                {
                    return comparison;
                }
                return revisionA.getDate().compareTo(revisionB.getDate());
            }
        });

        // create change sets by author. ie: each change set object will contain
        // all of the changes made by a particular author.
        List<ChangeSet> changeSets = new LinkedList<ChangeSet>();
        ChangeSet changeSet = null;
        for (SimpleChange change : simpleChanges)
        {
            if (changeSet == null)
            {
                changeSet = new ChangeSet(change);
            }
            else
            {
                if (changeSet.belongsTo(change))
                {
                    changeSet.add(change);
                } else
                {
                    changeSets.add(changeSet);
                    changeSet = new ChangeSet(change);
                }
            }
        }
        if (changeSet != null)
        {
            changeSets.add(changeSet);
        }

        // refine the changesets, taking splitting it up according to the rules
        // defined above.
        List<ChangeSet> refinedSets = new LinkedList<ChangeSet>();
        for (ChangeSet set : changeSets)
        {
            refinedSets.addAll(set.refine());
        }

        // now that we have the changeset information, lets create the final product.
        List<Changelist> changelists = new LinkedList<Changelist>();
        for (ChangeSet set : refinedSets)
        {
            // convert the changeset into a list of changes.
            List<SimpleChange> changes = new LinkedList<SimpleChange>();
            for (SimpleChange c : set.getChanges())
            {
                changes.add(c);
            }


            CvsRevision firstRevision = null;//(CvsRevision) changes.get(0).getRevision();
            SimpleChangelist changelist = new SimpleChangelist(firstRevision, firstRevision.getDate(), firstRevision.getAuthor(), firstRevision.getComment());
            for (SimpleChange change : changes)
            {
                changelist.addChange(change);
            }
            changelists.add(changelist);
        }

        return changelists;

    }

    private Map<String, LogInformation> getLogInfo(List<String> modifiedFiles) throws SCMException
    {
        Connection connection = null;
        try
        {
            GlobalOptions globalOptions = new GlobalOptions();
            globalOptions.setCVSRoot(cvsRoot.toString());

            connection = ConnectionFactory.getConnection(cvsRoot);
            connection.open();

            final Map<String, LogInformation> infos = new HashMap<String, LogInformation>();
            final List<LogInformation> rlogResponse = new LinkedList<LogInformation>();

            Client client = new Client(connection, new StandardAdminHandler());

            // the local path is not important for the RLogCommand, but needs to exist... go figure..
            client.setLocalPath("a:/some/local/path");

            client.getEventManager().addCVSListener(new CVSAdapter()
            {
                public void fileInfoGenerated(FileInfoEvent e)
                {
                    LogInformation info = (LogInformation) e.getInfoContainer();
                    rlogResponse.add(info);
                }
            }
            );

            RlogCommand log = new RlogCommand();
            for (String f : modifiedFiles)
            {
                log.setModule(f);
            }

            client.executeCommand(log, globalOptions);

            // match up the log info with the modified file names.
            assert(rlogResponse.size() == modifiedFiles.size());

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
            CVSHelper.close(connection);
        }
    }

    /**
     * Retrieve the history info from the remote system.
     *
     * @return
     * @throws SCMException
     */
    private List<HistoryInformation> getHistoryInfo(Date sinceDate) throws SCMException
    {
        Connection connection = null;
        try
        {
            GlobalOptions globalOptions = new GlobalOptions();
            globalOptions.setCVSRoot(cvsRoot.toString());

            connection = ConnectionFactory.getConnection(cvsRoot);
            connection.open();

            Client client = new Client(connection, new StandardAdminHandler());

            HistoryBuilder builder = new HistoryBuilder();
            client.getEventManager().addCVSListener(new BuilderAdapter(builder));

            HistoryCommand history = new HistoryCommand();
            history.setReportCommits(true);
            history.setForAllUsers(true);
            if (sinceDate != null)
            {
                history.setSinceDate(DATE_FORMAT.format(sinceDate));
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
            // cleanup any resources used by this command.
            CVSHelper.close(connection);
        }
    }

    class ChangeSet
    {
        private final List<SimpleChange> changes = new LinkedList<SimpleChange>();

        ChangeSet(SimpleChange c)
        {
            changes.add(c);
        }

        void add(SimpleChange c)
        {
            changes.add(c);
        }

        boolean belongsTo(SimpleChange other)
        {
            CvsRevision otherRevision = null;//(CvsRevision) other.getRevision();
            SimpleChange previousCommit = changes.get(0);
            CvsRevision revision = null;//(CvsRevision) previousCommit.getRevision();
            return revision.getAuthor().equals(otherRevision.getAuthor()) &&
                    revision.getBranch().equals(otherRevision.getBranch()) &&
                    revision.getComment().equals(otherRevision.getComment());
        }

        public List<ChangeSet> refine()
        {
            Map<String, String> filenames = new HashMap<String, String>();
            List<ChangeSet> changesets = new LinkedList<ChangeSet>();

            ChangeSet changeSet = null;
            String message = null;
            for (SimpleChange c : changes)
            {
                CvsRevision r = null;//(CvsRevision) c.getRevision();
                if (filenames.containsKey(c.getFilename()))
                {
                    // time for a new changeset.
                    filenames.clear();
                    changesets.add(changeSet);
                    filenames.put(c.getFilename(), c.getFilename());
                    changeSet = new ChangeSet(c);
                } else
                {
                    if (message == null)
                    {
                        message = r.getComment();
                    }
                    if (!message.equals(r.getComment()))
                    {
                        // time for a new changeset.
                        filenames.clear();
                        message = null;
                        changesets.add(changeSet);
                        filenames.put(c.getFilename(), c.getFilename());
                        changeSet = new ChangeSet(c);
                    } else
                    {

                        // keep on going.
                        filenames.put(c.getFilename(), c.getFilename());
                        if (changeSet == null)
                        {
                            changeSet = new ChangeSet(c);
                        } else
                        {
                            changeSet.add(c);
                        }
                    }
                }
            }
            if (changeSet != null)
            {
                changesets.add(changeSet);
            }
            return changesets;
        }

        public List<SimpleChange> getChanges()
        {
            return changes;
        }
    }
}
