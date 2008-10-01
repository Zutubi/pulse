package com.zutubi.pulse.core.scm.git;

import com.zutubi.pulse.core.ExecutionContext;
import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.scm.*;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Implementation of the {@link com.zutubi.pulse.core.scm.ScmClient} interface for the
 * Git source control system (http://git.or.cz/).
 */
public class GitClient implements ScmClient
{
    private static final Revision HEAD = new Revision("HEAD");
    private static final String LOCAL_BRANCH_NAME = "local";
    private static final String TMP_BRANCH_PREFIX = "tmp.";
    private static final String FLAG_EDITED =   "M";
    private static final String FLAG_ADDED =    "A";
    private static final String FLAG_DELETED =  "D";

    private static final Set<ScmCapability> CAPABILITIES = new HashSet<ScmCapability>();

    static
    {
        CAPABILITIES.add(ScmCapability.BROWSE);
        CAPABILITIES.add(ScmCapability.CHANGESETS);
        CAPABILITIES.add(ScmCapability.POLL);
        CAPABILITIES.add(ScmCapability.REVISIONS);
    }

    private static final Map<String, Change.Action> LOG_ACTION_MAPPINGS = new HashMap<String, Change.Action>();

    static
    {
        LOG_ACTION_MAPPINGS.put(FLAG_ADDED, Change.Action.ADD);
        LOG_ACTION_MAPPINGS.put(FLAG_EDITED, Change.Action.EDIT);
        LOG_ACTION_MAPPINGS.put(FLAG_DELETED, Change.Action.DELETE);
    }

    private String repository;
    private String branch;

    public GitClient(String repository, String branch)
    {
        this.repository = repository;
        this.branch = branch;
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

    public Revision checkout(ExecutionContext context, Revision revision, ScmEventHandler handler) throws ScmException
    {
        NativeGit git = new NativeGit();

        File workingDir = context.getWorkingDir();
        // Git likes to create the directory we clone into, so we need to ensure that it can do so.
        if (workingDir.exists() && !FileSystemUtils.rmdir(workingDir))
        {
            throw new ScmException("Failed in clean checkout.  Could not delete directory: " + workingDir.getAbsolutePath());
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

        Revision evaluatedRevision = new Revision(entry.getAuthor(), entry.getComment(), entry.getDate(), entry.getId());
        evaluatedRevision.setBranch(branch);
        return evaluatedRevision;
    }

    public Revision update(ExecutionContext context, Revision revision, ScmEventHandler handler) throws ScmException
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

        Revision evaluatedRevision = new Revision(entry.getAuthor(), entry.getComment(), entry.getDate(), entry.getId());
        evaluatedRevision.setBranch(branch);
        return evaluatedRevision;
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

    public FileStatus.EOLStyle getEOLPolicy(ScmContext context) throws ScmException
    {
        return FileStatus.EOLStyle.BINARY;
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

            Revision rev = new Revision(entry.getAuthor(), entry.getComment(), entry.getDate(), entry.getId());
            rev.setBranch(branch);
            return rev;
        }
    }

    private void preparePersistentDirectory(File workingDir) throws ScmException
    {
        NativeGit git = new NativeGit();
        if (new File(workingDir, ".git").isDirectory())
        {
            git.setWorkingDirectory(workingDir);
            git.checkout(null, LOCAL_BRANCH_NAME);
            git.pull(null);
        }
        else
        {
            // git does not like a checkouts into existing directories - not this way anyways.
            if (workingDir.exists() && !FileSystemUtils.rmdir(workingDir))
            {
                throw new ScmException("Failed checkout.  Could not delete directory: " + workingDir.getAbsolutePath());
            }

            git.setWorkingDirectory(workingDir.getParentFile());
            git.clone(null, repository, workingDir.getName());

            git.setWorkingDirectory(workingDir);
            git.checkout(null, "origin/" + branch, LOCAL_BRANCH_NAME);
        }
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
                Revision rev = new Revision(entry.getAuthor(), entry.getComment(), entry.getDate(), entry.getId());
                rev.setBranch(branch);
                revisions.add(rev);
            }
            return revisions;
        }
    }

    public List<Changelist> getChanges(ScmContext context, Revision from, Revision to) throws ScmException
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

            // be aware, the log contains duplicate file entries for the edit/delete case.

            List<Changelist> changelists = new LinkedList<Changelist>();
            for (GitLogEntry entry : entries)
            {
                Revision rev = new Revision(entry.getAuthor(), entry.getComment(), entry.getDate(), entry.getId());
                rev.setBranch(branch);

                Changelist changelist = new Changelist(rev);

                for (GitLogEntry.FileChangeEntry file : entry.getFiles())
                {
                    Change.Action action = Change.Action.UNKNOWN;
                    if (LOG_ACTION_MAPPINGS.containsKey(file.getAction()))
                    {
                        action = LOG_ACTION_MAPPINGS.get(file.getAction());
                    }
                    
                    Change change = new Change(file.getName(), entry.getId(), action);
                    changelist.addChange(change);
                }
                changelists.add(changelist);
            }

            return changelists;
        }
    }

    public List<ScmFile> browse(ScmContext context, String path, Revision revision) throws ScmException
    {
        synchronized (context)
        {
            File workingDir = context.getPersistentWorkingDir();

            preparePersistentDirectory(workingDir);

            if (revision != null)
            {
                // reset to the requested revision.  We expect the requested revision to be in the log.
                NativeGit git = new NativeGit();
                git.setWorkingDirectory(workingDir);
                git.checkout(null, revision.getRevisionString(), TMP_BRANCH_PREFIX + revision.getRevisionString());
            }

            ScmFile parent = new ScmFile(path);
            File base = new File(workingDir, path);
            if (base.isFile())
            {
                return Arrays.asList(new ScmFile(path));
            }
            List<ScmFile> listing = new LinkedList<ScmFile>();
            if (base.isDirectory())
            {
                for (File file : base.listFiles())
                {
                    ScmFile f = new ScmFile(parent, file.getName(), file.isDirectory());
                    listing.add(f);
                }
            }
            return listing;
        }
    }

    public void tag(ExecutionContext context, Revision revision, String name, boolean moveExisting) throws ScmException
    {
        // not yet implemented.
    }

    public Revision parseRevision(String revision) throws ScmException
    {
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
