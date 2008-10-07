package com.zutubi.pulse.core.scm.git;

import com.zutubi.pulse.core.ExecutionContext;
import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.TextUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Implementation of the {@link com.zutubi.pulse.core.scm.api.ScmClient} interface for the
 * GIT source control system (http://git.or.cz/).
 */
public class GitClient implements ScmClient
{
    private static final Set<ScmCapability> CAPABILITIES = new HashSet<ScmCapability>();

    static
    {
        CAPABILITIES.add(ScmCapability.BROWSE);
        CAPABILITIES.add(ScmCapability.CHANGESETS);
        CAPABILITIES.add(ScmCapability.POLL);
        CAPABILITIES.add(ScmCapability.REVISIONS);
    }

    private String repository;
    private String branch;
    private static final String LOCAL_BRANCH_NAME = "local";
    private static final String TMP_BRANCH_PREFIX = "tmp.";
    private static final Revision HEAD = new Revision("HEAD");
    private static final String FLAG_EDITED =   "M";
    private static final String FLAG_ADDED =    "A";
    private static final String FLAG_DELETED =  "D";

    /**
     * @see com.zutubi.pulse.core.scm.api.ScmClient#close()
     */
    public void close()
    {
        // noop.
    }

    /**
     * @see com.zutubi.pulse.core.scm.api.ScmClient#getCapabilities()
     */
    public Set<ScmCapability> getCapabilities()
    {
        return CAPABILITIES;
    }

    /**
     * @see com.zutubi.pulse.core.scm.api.ScmClient#getUid()
     */
    public String getUid() throws ScmException
    {
        return repository;
    }

    /**
     * @see com.zutubi.pulse.core.scm.api.ScmClient#getLocation()
     */
    public String getLocation() throws ScmException
    {
        return getUid();
    }

    /**
     * @see com.zutubi.pulse.core.scm.api.ScmClient#checkout(com.zutubi.pulse.core.ExecutionContext,com.zutubi.pulse.core.scm.api.Revision,com.zutubi.pulse.core.scm.api.ScmEventHandler)
     */
    public Revision checkout(ExecutionContext context, Revision revision, ScmEventHandler handler) throws ScmException
    {
        NativeGit git = new NativeGit();
        git.setScmEventHandler(handler);

        File workingDir = context.getWorkingDir();
        // Git likes to create the directory we clone into, so we need to ensure that it can do so.
        if (workingDir.exists() && !FileSystemUtils.rmdir(workingDir))
        {
            throw new ScmException("Failed in clean checkout.  Could not delete directory: " + workingDir.getAbsolutePath());
        }

        git.setWorkingDirectory(workingDir.getParentFile());
        git.clone(repository, workingDir.getName());

        git.setWorkingDirectory(workingDir);
        git.checkout("origin/" + branch, LOCAL_BRANCH_NAME);

        if (revision != null && TextUtils.stringSet(revision.getRevisionString()))
        {
            String rev = revision.getRevisionString();
            git.checkout(rev, TMP_BRANCH_PREFIX + rev);
        }

        // todo: if we want to provide extra feedback on the checkout, we run the checkout and then traverse the files, reporting them all as added.

        GitLogEntry entry = git.log(1).get(0);

        return new Revision(entry.getId());
    }

    /**
     * @see com.zutubi.pulse.core.scm.api.ScmClient#update(com.zutubi.pulse.core.ExecutionContext,com.zutubi.pulse.core.scm.api.Revision,com.zutubi.pulse.core.scm.api.ScmEventHandler)
     */
    public Revision update(ExecutionContext context, Revision revision, ScmEventHandler handler) throws ScmException
    {
        File workingDir = context.getWorkingDir();

        NativeGit git = new NativeGit();
        git.setScmEventHandler(handler);
        git.setWorkingDirectory(workingDir);

        // switch to the primary local checkout and update.
        git.checkout(LOCAL_BRANCH_NAME);

        // todo: determine the changes pulled in for the scm handler.  Get initial revision, pull, get final revision, then diff the two.

        git.pull();

        // cleanup any existing tmp local branches.
        List<GitBranchEntry> branches = git.branch();
        for (GitBranchEntry branch : branches)
        {
            if (branch.getName().startsWith(TMP_BRANCH_PREFIX))
            {
                git.deleteBranch(branch.getName());
            }
        }

        if (revision != null && TextUtils.stringSet(revision.getRevisionString()))
        {
            String rev = revision.getRevisionString();
            git.checkout(rev, TMP_BRANCH_PREFIX + rev);
        }

        git.setWorkingDirectory(workingDir);
        GitLogEntry entry = git.log(1).get(0);

        return new Revision(entry.getId());
    }

    /**
     * @see com.zutubi.pulse.core.scm.api.ScmClient#retrieve(com.zutubi.pulse.core.scm.api.ScmContext,String,com.zutubi.pulse.core.scm.api.Revision)
     */
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

    /**
     * @see com.zutubi.pulse.core.scm.api.ScmClient#storeConnectionDetails(java.io.File)
     */
    public void storeConnectionDetails(File outputDir) throws ScmException, IOException
    {

    }

    /**
     * @see com.zutubi.pulse.core.scm.api.ScmClient#getEOLPolicy(com.zutubi.pulse.core.scm.api.ScmContext)
     */
    public EOLStyle getEOLPolicy(ScmContext context) throws ScmException
    {
        return EOLStyle.BINARY;
    }

    /**
     * @see com.zutubi.pulse.core.scm.api.ScmClient#getLatestRevision(com.zutubi.pulse.core.scm.api.ScmContext)
     */
    public Revision getLatestRevision(ScmContext context) throws ScmException
    {
        synchronized (context)
        {
            File workingDir = context.getPersistentWorkingDir();

            preparePersistentDirectory(workingDir);

            // need to handle the case where this is the first revision...
            NativeGit git = new NativeGit();
            git.setWorkingDirectory(workingDir);

            GitLogEntry entry = git.log(1).get(0);

            return new Revision(entry.getId());
        }
    }

    private void preparePersistentDirectory(File workingDir) throws ScmException
    {
        NativeGit git = new NativeGit();
        if (new File(workingDir, ".git").isDirectory())
        {
            git.setWorkingDirectory(workingDir);
            git.checkout(LOCAL_BRANCH_NAME);
            git.pull();
        }
        else
        {
            // git does not like a checkouts into existing directories - not this way anyways.
            if (workingDir.exists() && !FileSystemUtils.rmdir(workingDir))
            {
                throw new ScmException("Failed in clean checkout.  Could not delete directory: " + workingDir.getAbsolutePath());
            }

            git.setWorkingDirectory(workingDir.getParentFile());
            git.clone(repository, workingDir.getName());

            git.setWorkingDirectory(workingDir);
            git.checkout("origin/" + branch, LOCAL_BRANCH_NAME);
        }
    }

    /**
     * @see com.zutubi.pulse.core.scm.api.ScmClient#getRevisions(com.zutubi.pulse.core.scm.api.ScmContext,com.zutubi.pulse.core.scm.api.Revision,com.zutubi.pulse.core.scm.api.Revision)
     */
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

    /**
     * @see com.zutubi.pulse.core.scm.api.ScmClient#getChanges(com.zutubi.pulse.core.scm.api.ScmContext,com.zutubi.pulse.core.scm.api.Revision,com.zutubi.pulse.core.scm.api.Revision)
     */
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
                Revision rev = new Revision(entry.getId());

                Changelist changelist = new Changelist(rev);

                for (GitLogEntry.FileChangeEntry file : entry.getFiles())
                {
                    Change.Action action;
                    if (file.getAction().equals(FLAG_EDITED))
                    {
                        action = Change.Action.EDIT;
                    }
                    else if (file.getAction().equals(FLAG_DELETED))
                    {
                        action = Change.Action.DELETE;
                    }
                    else if (file.getAction().equals(FLAG_ADDED))
                    {
                        action = Change.Action.ADD;
                    }
                    else
                    {
                        action = Change.Action.UNKNOWN;
                    }
                    
                    Change change = new Change(file.getName(), entry.getId(), action);
                    changelist.addChange(change);
                }
                changelists.add(changelist);
            }

            return changelists;
        }
    }

    /**
     * @see com.zutubi.pulse.core.scm.api.ScmClient#browse(com.zutubi.pulse.core.scm.api.ScmContext,String,com.zutubi.pulse.core.scm.api.Revision)
     */
    public List<ScmFile> browse(ScmContext context, String path, Revision revision) throws ScmException
    {
        synchronized (context)
        {
            File workingDir = context.getPersistentWorkingDir();

            preparePersistentDirectory(workingDir);

            if (revision != null && TextUtils.stringSet(revision.getRevisionString()))
            {
                // reset to the requested revision.  We expect the requested revision to be in the log.
                String rev = revision.getRevisionString();

                NativeGit git = new NativeGit();
                git.setWorkingDirectory(workingDir);
                git.checkout(rev, TMP_BRANCH_PREFIX + rev);
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

    /**
     * @see com.zutubi.pulse.core.scm.api.ScmClient#tag(com.zutubi.pulse.core.ExecutionContext,com.zutubi.pulse.core.scm.api.Revision,String,boolean)
     */
    public void tag(ExecutionContext context, Revision revision, String name, boolean moveExisting) throws ScmException
    {
        // not yet implemented.
    }

    /**
     * @see com.zutubi.pulse.core.scm.api.ScmClient#parseRevision(String)
     */
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
