package com.zutubi.pulse.core.scm.git;

import com.zutubi.pulse.core.ExecutionContext;
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
    private static final String LOCAL_BRANCH_NAME = "local";
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
        LOG_ACTION_MAPPINGS.put(ACTION_EDITED, FileChange.Action.EDIT);
        LOG_ACTION_MAPPINGS.put(ACTION_DELETED, FileChange.Action.DELETE);
    }

    private String repository;
    private String branch;

    public GitClient(String repository, String branch)
    {
        this.repository = repository;
        this.branch = branch;
    }


    public void init(ScmContext context) throws ScmException
    {
        preparePersistentDirectory(context.getPersistentWorkingDir());
    }

    public void close()
    {
        // noop.
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
        NativeGit git = new NativeGit();

        File workingDir = context.getWorkingDir();
        // Git likes to create the directory we clone into, so we need to ensure that it can do so.
        if (workingDir.exists() && !FileSystemUtils.rmdir(workingDir))
        {
            throw new ScmException("Checkout failed. Could not delete directory: " + workingDir.getAbsolutePath());
        }

        git.setWorkingDirectory(workingDir.getParentFile());
        git.clone(handler, repository, workingDir.getName());

        git.setWorkingDirectory(workingDir);
        git.checkout(handler, "origin/" + branch, LOCAL_BRANCH_NAME);

        if (revision != null)
        {
            git.checkout(handler, revision.getRevisionString(), TMP_BRANCH_PREFIX + revision.getRevisionString());
        }

        // todo: if we want to provide extra feedback on the checkout, we run the checkout and then traverse the files, reporting them all as added.

        // Determine the head revision from this checkout.  This is equivalent to the evaluated version
        // revision parameter which may be a relative revision (HEAD~4 for instance).
        GitLogEntry entry = git.log(1).get(0);

        return new Revision(entry.getId());
    }

    public Revision update(ExecutionContext context, Revision revision, ScmFeedbackHandler handler) throws ScmException
    {
        File workingDir = context.getWorkingDir();

        NativeGit git = new NativeGit();
        git.setWorkingDirectory(workingDir);

        // switch to the primary local checkout and update.
        git.checkout(handler, LOCAL_BRANCH_NAME);

        // todo: determine the changes pulled in for the scm handler.  Get initial revision, pull, get final revision, then diff the two.

        git.pull(handler);

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

        git.setWorkingDirectory(workingDir);
        GitLogEntry entry = git.log(1).get(0);

        return new Revision(entry.getId());
    }

    public InputStream retrieve(ScmContext context, String path, Revision revision) throws ScmException
    {
        synchronized (context)
        {
            File workingDir = context.getPersistentWorkingDir();

            preparePersistentDirectory(workingDir);

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

            preparePersistentDirectory(workingDir);

            NativeGit git = new NativeGit();
            git.setWorkingDirectory(workingDir);

            GitLogEntry entry = git.log(1).get(0);

            return new Revision(entry.getId());
        }
    }

    private void preparePersistentDirectory(File workingDir) throws ScmException
    {
        ScmFeedbackHandler handler = new ScmFeedbackAdapter();

        NativeGit git = new NativeGit();
        if (isGitRepository(workingDir))
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
        else
        {
            // git does not like a checkouts into existing directories - not this way anyways.
            if (workingDir.exists() && !FileSystemUtils.rmdir(workingDir))
            {
                throw new ScmException("Checkout failed. Could not delete directory: " + workingDir.getAbsolutePath());
            }

            git.setWorkingDirectory(workingDir.getParentFile());
            git.clone(handler, repository, workingDir.getName());

            git.setWorkingDirectory(workingDir);
            git.checkout(handler, "origin/" + branch, LOCAL_BRANCH_NAME);
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

            preparePersistentDirectory(workingDir);

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

            preparePersistentDirectory(workingDir);

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
