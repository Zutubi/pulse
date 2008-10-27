package com.zutubi.pulse.core.scm.git;

import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.scm.api.*;
import static com.zutubi.pulse.core.scm.git.GitConstants.*;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.TextUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Implementation of the {@link com.zutubi.pulse.core.scm.api.ScmClient} interface for the
 * Git source control system (http://git.or.cz/).
 */
public class GitClient implements ScmClient
{
    private static final Revision HEAD = new Revision("HEAD");
    /**
     * The name of the local branch used by scm context operations.
     */
    private static final String LOCAL_BRANCH_NAME = "local";
    /**
     * The prefix applied to temporary branch names, used when checking out
     * a specific revision.  Temporary branches exist for the duration of the
     * operation and are subsequently deleted.
     */
    private static final String TMP_BRANCH_PREFIX = "tmp.";

    private static final Set<ScmCapability> CAPABILITIES = new HashSet<ScmCapability>();

    static
    {
        CAPABILITIES.add(ScmCapability.CHANGESETS);
        CAPABILITIES.add(ScmCapability.POLL);
        CAPABILITIES.add(ScmCapability.REVISIONS);
    }

    private static final Map<String, FileChange.Action> LOG_ACTION_MAPPINGS = new HashMap<String, FileChange.Action>();

    static
    {
        LOG_ACTION_MAPPINGS.put(ACTION_ADDED, FileChange.Action.ADD);
        LOG_ACTION_MAPPINGS.put(ACTION_MODIFIED, FileChange.Action.EDIT);
        LOG_ACTION_MAPPINGS.put(ACTION_DELETED, FileChange.Action.DELETE);
        LOG_ACTION_MAPPINGS.put(ACTION_RENAME_MODIFIED, FileChange.Action.MOVE);

        // the following two dont have direct mappings to our internal understanding
        // of file changes.  Should we add specific actions for them?.
        LOG_ACTION_MAPPINGS.put(ACTION_COPY_MODIFIED, FileChange.Action.UNKNOWN);
        LOG_ACTION_MAPPINGS.put(ACTION_UNMERGED, FileChange.Action.UNKNOWN);
    }

    /**
     * The source repository from which the data will be retrieved.
     */
    private String repository;

    /**
     * The source repositories branch name.
     */
    private String branch;

    public GitClient(String repository, String branch)
    {
        this.repository = repository;
        this.branch = branch;
    }

    /**
     * Prepare the local clone of the remote git repository.  This local clone will subsequently
     * be used for browsing, checking for changes, determining changelists etc etc.
     *
     * @param context the scm context in which this git client will be operating.
     * @throws ScmException if we encounter a problem
     */
    public void init(ScmContext context) throws ScmException
    {
        synchronized(context)
        {
            // at this stage, we are not overly concerned with feedback since this is running
            // in the background, so use the default noop handler.
            ScmFeedbackHandler handler = new ScmFeedbackAdapter();

            File workingDir = context.getPersistentWorkingDir();

            // git does not like to clone 'into' existing directories.
            if (workingDir.exists() && !FileSystemUtils.rmdir(workingDir))
            {
                throw new ScmException("Init failed. Could not delete directory: " + workingDir.getAbsolutePath());
            }

            NativeGit git = new NativeGit();
            git.setWorkingDirectory(workingDir.getParentFile());
            // git clone -n <repository> dir
            git.clone(handler, repository, workingDir.getName());

            // cd into git repository.
            git.setWorkingDirectory(workingDir);

            // git checkout -b local origin/<branch>
            git.checkout(handler, "origin/" + branch, LOCAL_BRANCH_NAME);
        }
    }

    public void close()
    {
        // noop.  We do not keep any processes active, and the persistent directory
        // remains for the duration of the scm configuration.
    }

    public Set<ScmCapability> getCapabilities()
    {
        return CAPABILITIES;
    }

    public String getUid() throws ScmException
    {
        return repository;
    }

    public String getLocation() throws ScmException
    {
        return getUid();
    }

    public Revision checkout(ExecutionContext context, Revision revision, ScmFeedbackHandler handler) throws ScmException
    {
        // IMPLEMENTATION NOTE:
        //  we can improve the speed of the checkout by running git clone -depth 1, thereby not downloading all of the
        // repositories history.  However, given the current interaction between the scm client and pulse, the working
        // directory created by the checkout may be used subsequently for an update, or multiple updates, as well as
        // an update to an old revision.  This would not be possible with a restricted history repository.  So, we go
        // with the slower checkout for now.  If users have issues with the speed of the checkout in builds, they
        // should select an alternate checkout scheme - both CLEAN_UPDATE and INCREMENTAL_UPDATE would do the trick. 

        File workingDir = context.getWorkingDir();
        // Git likes to create the directory we clone into, so we need to ensure that it can do so.
        if (workingDir.exists() && !FileSystemUtils.rmdir(workingDir))
        {
            throw new ScmException("Checkout failed. Could not delete directory: " + workingDir.getAbsolutePath());
        }

        NativeGit git = new NativeGit();
        git.setWorkingDirectory(workingDir.getParentFile());
        // git clone -n <repository> dir
        git.clone(handler, repository, workingDir.getName());

        // cd workingDir
        git.setWorkingDirectory(workingDir);
        git.checkout(handler, "origin/" + branch, LOCAL_BRANCH_NAME);

        // if we are after a specific revision, check it out to a temporary branch.  This also updates
        // the working copy to that branch.
        if (revision != null)
        {
            git.checkout(handler, revision.getRevisionString(), TMP_BRANCH_PREFIX + revision.getRevisionString());
        }

        // feedback can be determined by using git diff with the appropriate properties.  We can not
        // get feedback from the checkout process itself, and so any feedback generated is delayed.  
        try
        {
            git.diff(handler, revision);
        }
        catch (GitException e)
        {
            // we are making a guess at a non-existant revision here.
        }

        // Determine the head revision from this checkout.  This is equivalent to the evaluated version
        // revision parameter which may be a relative revision (HEAD~4 for instance).
        GitLogEntry entry = git.log(1).get(0);

        return new Revision(entry.getId());
    }

    public Revision update(ExecutionContext context, Revision revision, ScmFeedbackHandler handler) throws ScmException
    {
        File workingDir = context.getWorkingDir();
        if (!isGitRepository(workingDir))
        {
            throw new ScmException("");
        }

        NativeGit git = new NativeGit();
        git.setWorkingDirectory(workingDir);

        // switch to the primary local checkout and update.
        git.checkout(handler, LOCAL_BRANCH_NAME);

        // - get the current revision on head.
        // - pull
        // - get the new revision on head
        // - run a diff to provide feedback.

        String fromRevision = git.log(1).get(0).getId();
        git.pull(handler);
        String toRevision = git.log(1).get(0).getId();
        if (fromRevision.compareTo(toRevision) != 0)
        {
            git.diff(handler, new Revision(fromRevision), new Revision(toRevision));
        }


        // cleanup any existing tmp local branches.
        List<GitBranchEntry> branches = git.branch();
        for (GitBranchEntry branch : branches)
        {
            if (branch.getName().startsWith(TMP_BRANCH_PREFIX))
            {
                git.deleteBranch(branch.getName());
            }
        }

        if (revision != null)
        {
            String rev = revision.getRevisionString();
            git.checkout(handler, rev, TMP_BRANCH_PREFIX + rev);
        }

        GitLogEntry entry = git.log(1).get(0);
        return new Revision(entry.getId());
    }

    public InputStream retrieve(ScmContext context, String path, Revision revision) throws ScmException
    {
        synchronized (context)
        {
            File workingDir = context.getPersistentWorkingDir();

            preparePersistentDirectory(context, workingDir);

            NativeGit git = new NativeGit();
            git.setWorkingDirectory(workingDir);

            if (revision == null)
            {
                return git.show(path);
            }
            else
            {
                return git.show(revision.getRevisionString(), path);
            }
        }
    }

    public void storeConnectionDetails(File outputDir) throws ScmException, IOException
    {

    }

    public EOLStyle getEOLPolicy(ScmContext context) throws ScmException
    {
        return EOLStyle.BINARY;
    }

    public Revision getLatestRevision(ScmContext context) throws ScmException
    {
        synchronized (context)
        {
            File workingDir = context.getPersistentWorkingDir();

            preparePersistentDirectory(context, workingDir);

            NativeGit git = new NativeGit();
            git.setWorkingDirectory(workingDir);

            GitLogEntry entry = git.log(1).get(0);

            return new Revision(entry.getId());
        }
    }

    private void preparePersistentDirectory(ScmContext context, File workingDir) throws ScmException
    {
        NativeGit git = new NativeGit();
        if (!isGitRepository(workingDir))
        {
            try
            {
                throw new ScmException("Git repository not found: " + workingDir.getCanonicalPath());
            }
            catch (IOException e)
            {
                throw new ScmException("Git repository not found: " + workingDir.getAbsolutePath());
            }
        }
        else
        {
            git.setWorkingDirectory(workingDir);

            boolean localBranchExists = false;
            for (GitBranchEntry branch : git.branch())
            {
               if (LOCAL_BRANCH_NAME.equals(branch.getName()))
               {
                   localBranchExists = true;
                   break;
               }
            }

            ScmFeedbackHandler handler = new ScmFeedbackAdapter();

            if (localBranchExists)
            {
                git.checkout(handler, LOCAL_BRANCH_NAME);
                git.pull(handler);
            }
            else
            {
                git.checkout(handler, "origin/" + branch, LOCAL_BRANCH_NAME);
            }
        }
    }

    private boolean isGitRepository(File dir)
    {
        return new File(dir, ".git").isDirectory();
    }

    public List<Revision> getRevisions(ScmContext context, Revision from, Revision to) throws ScmException
    {
        synchronized (context)
        {
            File workingDir = context.getPersistentWorkingDir();

            preparePersistentDirectory(context, workingDir);

            if (to == null)
            {
                to = HEAD;
            }

            NativeGit git = new NativeGit();
            git.setWorkingDirectory(workingDir);

            List<GitLogEntry> entries = git.log(from.getRevisionString(), to.getRevisionString());

            List<Revision> revisions = new LinkedList<Revision>();
            for (GitLogEntry entry : entries)
            {
                revisions.add(new Revision(entry.getId()));
            }
            return revisions;
        }
    }

    public List<Changelist> getChanges(ScmContext context, Revision from, Revision to) throws ScmException
    {
        synchronized (context)
        {
            if (to == null)
            {
                to = HEAD;
            }

            File workingDir = context.getPersistentWorkingDir();

            preparePersistentDirectory(context, workingDir);

            NativeGit git = new NativeGit();
            git.setWorkingDirectory(workingDir);

            List<GitLogEntry> entries = git.log(from.getRevisionString(), to.getRevisionString());

            // be aware, the log contains duplicate file entries for the edit/delete case.

            List<Changelist> changelists = new LinkedList<Changelist>();
            for (GitLogEntry entry : entries)
            {
                Revision rev = new Revision(entry.getId());

                List<FileChange> changes = new LinkedList<FileChange>();
                for (GitLogEntry.FileChangeEntry file : entry.getFiles())
                {
                    FileChange.Action action = FileChange.Action.UNKNOWN;
                    if (LOG_ACTION_MAPPINGS.containsKey(file.getAction()))
                    {
                        action = LOG_ACTION_MAPPINGS.get(file.getAction());
                    }

                    FileChange change = new FileChange(file.getName(), entry.getId(), action);
                    changes.add(change);
                }

                Changelist changelist = new Changelist(rev, entry.getDate().getTime(), entry.getAuthor(), entry.getComment(), changes);
                changelists.add(changelist);
            }

            return changelists;
        }
    }

    public List<ScmFile> browse(ScmContext context, String path, Revision revision) throws ScmException
    {
        // The fact that browse requires a local checkout is a problem.  Browse is available during the project
        // creation wizard, at point at which no persistent working directory is available (problem a).  The
        // second problem is that even if the directory was available, running a checkout at that point in time
        // could potentially take a while.  The UI will appear to hang as a result - not good.  So, until these
        // performance / timing issues are resolved, the browse capability has been disabled.

        return new LinkedList<ScmFile>();
    }

    public void tag(ExecutionContext context, Revision revision, String name, boolean moveExisting) throws ScmException
    {
        // not yet implemented.
    }

    public Revision parseRevision(String revision) throws ScmException
    {
        if (!TextUtils.stringSet(revision))
        {
            throw new ScmException("Unexpected git revision format: '" + revision + "'");
        }
        return new Revision(revision);
    }

    public void setRepository(String repository)
    {
        this.repository = repository;
    }

    public void setBranch(String branch)
    {
        this.branch = branch;
    }
}
