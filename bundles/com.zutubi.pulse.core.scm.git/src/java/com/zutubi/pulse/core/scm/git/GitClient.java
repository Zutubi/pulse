package com.zutubi.pulse.core.scm.git;

import com.google.common.base.Objects;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.engine.api.ResourceProperty;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.core.scm.git.config.GitConfiguration;
import com.zutubi.pulse.core.scm.process.api.ScmLineHandler;
import com.zutubi.pulse.core.scm.process.api.ScmOutputCapturingHandler;
import com.zutubi.util.StringUtils;
import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.reverse;
import static com.zutubi.pulse.core.scm.git.GitConstants.*;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Implementation of the {@link com.zutubi.pulse.core.scm.api.ScmClient} interface for the
 * Git source control system (http://git.or.cz/).
 */
public class GitClient implements ScmClient
{
    public static final String TYPE = "git";
    
    private static final Logger LOG = Logger.getLogger(GitClient.class);

    static final Messages I18N = Messages.getInstance(GitClient.class);

    public static final String GIT_REPOSITORY_DIRECTORY = ".git";
    static final String KEY_INCOMPLETE_CHECKOUT = "incomplete.checkout.warning";
    static final String KEY_MERGE_ON_UPDATE = "merge.on.update.warning";
    /**
     * Marker file used to indicate a checkout completed.  Used to detect
     * incomplete checkouts (e.g. those that were cancelled part way through)
     * which may leave things in an unclean state.
     */
    static final String CHECKOUT_COMPLETE_FILENAME = ".pulse.git.complete";
    /**
     * Timeout for acquiring the ScmContext lock.
     */
    private static final int DEFAULT_TIMEOUT = 120;
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

    private static final Set<ScmCapability> CAPABILITIES = EnumSet.allOf(ScmCapability.class);

    private static final Map<String, FileChange.Action> LOG_ACTION_MAPPINGS = new HashMap<String, FileChange.Action>();

    static
    {
        LOG_ACTION_MAPPINGS.put(ACTION_ADDED, FileChange.Action.ADD);
        LOG_ACTION_MAPPINGS.put(ACTION_MODIFIED, FileChange.Action.EDIT);
        LOG_ACTION_MAPPINGS.put(ACTION_DELETED, FileChange.Action.DELETE);
        LOG_ACTION_MAPPINGS.put(ACTION_RENAME_MODIFIED, FileChange.Action.MOVE);

        // the following two don't have direct mappings to our internal understanding
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
    /**
     * Seconds of inactivity (lack of any output) from a git sub-process after
     * which that process should be timed out.  May be set to zero to indicate
     * no timeout should be applied.
     */
    private int inactivityTimeout;
    private GitConfiguration.CloneType cloneType;
    private int cloneDepth;
    private List<String> includedPaths = new LinkedList<String>();
    private List<String> excludedPaths = new LinkedList<String>();
    private boolean processSubmodules;
    private List<String> submoduleNames = Collections.emptyList();

    public GitClient(String repository, String branch, int inactivityTimeout, GitConfiguration.CloneType cloneType, int cloneDepth, boolean processSubmodules, List<String> submoduleNames)
    {
        this.repository = repository;
        this.branch = branch;
        this.inactivityTimeout = inactivityTimeout;
        this.cloneType = cloneType;
        this.cloneDepth = cloneDepth;
        this.processSubmodules = processSubmodules;
        this.submoduleNames = submoduleNames;
    }

    public String getImplicitResource()
    {
        return RESOURCE_NAME;
    }

    /**
     * Prepare the local clone of the remote git repository.  This local clone will subsequently
     * be used for browsing, checking for changes, determining changelists etc etc.
     *
     * @param context the scm context in which this git client will be operating.
     * @throws ScmException if we encounter a problem
     */
    public void init(ScmContext context, ScmFeedbackHandler handler) throws ScmException
    {
        File workingDir = context.getPersistentContext().getPersistentWorkingDir();

        // git does not like to clone 'into' existing directories.
        if (workingDir.exists())
        {
            try
            {
                FileSystemUtils.rmdir(workingDir);
            }
            catch (IOException e)
            {
                throw new ScmException("Init failed: " + e.getMessage(), e);
            }
        }

        NativeGit git = new NativeGit(inactivityTimeout, context.getEnvironmentContext());
        git.setWorkingDirectory(workingDir.getParentFile());
        // git clone -n <repository> dir
        handler.status("Initialising clone of git repository '" + repository + "'...");
        git.clone(handler, repository, workingDir.getName(), cloneType == GitConfiguration.CloneType.FULL_MIRROR, -1);
        handler.status("Repository cloned.");
    }

    public void destroy(ScmContext context, ScmFeedbackHandler handler) throws ScmException
    {
        // Nothing to do - the directory is deleted for us.
    }

    public void close()
    {
        // noop.  We do not keep any processes active, and the persistent directory
        // remains for the duration of the scm configuration.
    }

    public Set<ScmCapability> getCapabilities(ScmContext context)
    {
        if (context.getPersistentContext() != null)
        {
            return CAPABILITIES;
        }

        EnumSet<ScmCapability> capabilities = EnumSet.copyOf(CAPABILITIES);
        capabilities.remove(ScmCapability.BROWSE);
        return capabilities;
    }

    public String getUid(ScmContext context) throws ScmException
    {
        return repository;
    }

    public String getLocation(ScmContext context) throws ScmException
    {
        return getUid(context);
    }

    public List<ResourceProperty> getProperties(ExecutionContext context) throws ScmException
    {
        return Arrays.asList(
                new ResourceProperty("git.repository", repository),
                new ResourceProperty("git.branch", branch)
        );
    }

    public Revision checkout(ExecutionContext context, Revision revision, ScmFeedbackHandler handler) throws ScmException
    {
        File workingDir = context.getWorkingDir();

        // Git likes to create the directory we clone into, so we need to ensure that it can do so.
        // This also cleans up any previous partial checkout.
        if (workingDir.exists())
        {
            try
            {
                FileSystemUtils.rmdir(workingDir);
            }
            catch (IOException e)
            {
                throw new ScmException(e.getMessage(), e);
            }
        }

        NativeGit git = new NativeGit(inactivityTimeout, context);

        switch (cloneType)
        {
            case SHALLOW:
            {
                // git clone --no-checkout --depth <clone depth> <repository> <dir>
                git.setWorkingDirectory(workingDir.getParentFile());
                git.clone(handler, repository, workingDir.getName(), false, cloneDepth);
                git.setWorkingDirectory(workingDir);
                break;
            }
            case SELECTED_BRANCH_ONLY:
            {
                if (!workingDir.mkdir())
                {
                    throw new ScmException("Could not create directory '" + workingDir.getAbsolutePath() + "'");
                }

                git.setWorkingDirectory(workingDir);
                // git init
                // git remote add -f -t <branch> -m <branch> origin <repository>
                // git merge origin
                git.init(handler);
                git.remoteAdd(handler, REMOTE_ORIGIN, repository, branch);
                git.merge(handler, REMOTE_ORIGIN);
                break;
            }
            case NORMAL:
            {
                // git clone --no-checkout <repository> <dir>
                git.setWorkingDirectory(workingDir.getParentFile());
                git.clone(handler, repository, workingDir.getName(), false, -1);
                git.setWorkingDirectory(workingDir);
                break;
            }
            case FULL_MIRROR:
            {
                // git clone --mirror <repository> <dir>/.git
                if (!workingDir.mkdir())
                {
                    throw new ScmException("Could not create directory '" + workingDir.getAbsolutePath() + "'");
                }

                git.setWorkingDirectory(workingDir);
                git.clone(handler, repository, GIT_REPOSITORY_DIRECTORY, true, -1);
                git.config(handler, CONFIG_BARE, false);
                break;
            }
        }

        git.checkout(handler, getRemoteBranchRef(), LOCAL_BRANCH_NAME);

        // if we are after a specific revision, check it out to a temporary branch.  This also updates
        // the working copy to that branch.
        if (revision != null)
        {
            git.checkout(handler, revision.getRevisionString(), TMP_BRANCH_PREFIX + revision.getRevisionString());
        }

        if (processSubmodules)
        {
            git.submoduleUpdate(handler, submoduleNames.isEmpty(), submoduleNames.toArray(new String[submoduleNames.size()]));
        }

        createMarker(workingDir);

        // Determine the head revision from this checkout.  This is equivalent to the evaluated version
        // revision parameter which may be a relative revision (HEAD~4 for instance).
        List<GitLogEntry> logs = git.log(1);
        return new Revision(logs.get(0).getId());
    }

    File getMarkerFile(File workingDir)
    {
        File gitDir = new File(workingDir, GIT_REPOSITORY_DIRECTORY);
        return new File(gitDir, CHECKOUT_COMPLETE_FILENAME);
    }

    private boolean markerExists(File workingDir)
    {
        File marker = getMarkerFile(workingDir);
        if (marker.exists())
        {
            return true;
        }
        else
        {
            // For compatibility, support the old marker file location
            // (directly in the working directory).
            File oldMarker = new File(workingDir, CHECKOUT_COMPLETE_FILENAME);
            return oldMarker.exists();
        }
    }

    private void createMarker(File workingDir) throws ScmException
    {
        File marker = getMarkerFile(workingDir);
        try
        {
            if (!marker.createNewFile())
            {
                throw new ScmException("Could not create marker file.");
            }
        }
        catch (IOException e)
        {
            throw new ScmException("Could not create marker file: " + e.getMessage(), e);
        }
    }

    public Revision update(ExecutionContext context, Revision revision, ScmFeedbackHandler handler) throws ScmException
    {
        File workingDir = context.getWorkingDir();
        if (!isGitRepository(workingDir) || !markerExists(workingDir))
        {
            handler.status(I18N.format(KEY_INCOMPLETE_CHECKOUT));
            return checkout(context, revision, handler);
        }

        NativeGit git = new NativeGit(inactivityTimeout, context);
        git.setWorkingDirectory(workingDir);

        // switch to the primary local checkout and update.
        git.checkout(handler, LOCAL_BRANCH_NAME);

        // - get the current revision on head.
        // - fetch
        // - check merge is fast-forward
        // - merge
        // - get the new revision on head
        // - run a diff to provide feedback.

        String fromRevision = git.revisionParse(REVISION_HEAD);
        git.fetch(handler);
        String remote = getRemoteBranchRef();
        String mergeBaseSha = git.mergeBase(REVISION_HEAD, remote);
        if (!fromRevision.equals(mergeBaseSha))
        {
            handler.status(I18N.format(KEY_MERGE_ON_UPDATE));
            return checkout(context, revision, handler);
        }

        git.merge(handler, remote);

        String toRevision = git.revisionParse(REVISION_HEAD);
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

        if (processSubmodules)
        {
            git.submoduleUpdate(handler, submoduleNames.isEmpty(), submoduleNames.toArray(new String[submoduleNames.size()]));
        }

        GitLogEntry entry = git.log(1).get(0);
        return new Revision(entry.getId());
    }

    public InputStream retrieve(ScmContext context, String path, Revision revision) throws ScmException
    {
        PersistentContext persistentContext = context.getPersistentContext();
        persistentContext.tryLock(DEFAULT_TIMEOUT, SECONDS);
        try
        {
            NativeGit git = preparePersistentDirectory(context);
            return git.show(getRevisionString(revision), path);
        }
        finally
        {
            persistentContext.unlock();
        }
    }

    public void storeConnectionDetails(ExecutionContext context, File outputDir) throws ScmException, IOException
    {

    }

    public EOLStyle getEOLPolicy(ExecutionContext context) throws ScmException
    {
        return EOLStyle.BINARY;
    }

    public Revision getLatestRevision(ScmContext context) throws ScmException
    {
        PersistentContext persistentContext = context.getPersistentContext();
        persistentContext.tryLock(DEFAULT_TIMEOUT, SECONDS);
        try
        {
            NativeGit git = preparePersistentDirectory(context);
            GitLogEntry entry = git.log(getRemoteBranchRef(), 1).get(0);
            return new Revision(entry.getId());
        }
        finally
        {
            persistentContext.unlock();
        }
    }

    private NativeGit preparePersistentDirectory(ScmContext context) throws ScmException
    {
        File workingDir = context.getPersistentContext().getPersistentWorkingDir();
        NativeGit git = new NativeGit(inactivityTimeout, context.getEnvironmentContext());
        git.setFilterPaths(includedPaths, excludedPaths);
        if (!isGitRepository(workingDir))
        {
            String path;
            try
            {
                path = workingDir.getCanonicalPath();
            }
            catch (IOException e)
            {
                path = workingDir.getAbsolutePath();
            }

            throw new ScmException("Git repository not found: " + path);
        }
        else
        {
            git.setWorkingDirectory(workingDir);

            String remote = getRemoteBranchRef();
            
            // CIB-2986: getting the original revision will not work if the project was initialised
            // before the branch was created - we need to fetch before the branch is valid.  As that
            // is a reasonable use case, and the original revision is only needed to detect history
            // rewrites, we all this to fail and just do a straight fetch in that case.
            String originalSha = null;
            try
            {
                originalSha = git.revisionParse(remote);
            }
            catch (ScmException e)
            {
                LOG.debug("Unable to get revision before fetch, fast-forward detection disabled", e);
            }
            
            git.fetch(new ScmFeedbackAdapter());
            if (originalSha != null)
            {
                String newSha = git.revisionParse(remote);
                String mergeBaseSha = git.mergeBase(originalSha, newSha);
                if (!originalSha.equals(mergeBaseSha))
                {
                    GitException exception = new GitException("Fetch is not fast-forward, likely due to history changes upstream.  Project reinitialisation required.");
                    exception.setReinitialiseRequired();
                    throw exception;
                }
            }
            return git;
        }
    }

    private boolean isGitRepository(File dir)
    {
        return new File(dir, GIT_REPOSITORY_DIRECTORY).isDirectory();
    }

    public List<Revision> getRevisions(ScmContext context, Revision from, Revision to) throws ScmException
    {
        PersistentContext persistentContext = context.getPersistentContext();
        persistentContext.tryLock(DEFAULT_TIMEOUT, SECONDS);
        try
        {
            List<GitLogEntry> entries = gitlog(context, from, to);

            List<Revision> revisions = new LinkedList<Revision>();
            for (GitLogEntry entry : entries)
            {
                revisions.add(new Revision(entry.getId()));
            }
            return revisions;
        }
        finally
        {
            persistentContext.unlock();
        }
    }

    public List<Changelist> getChanges(ScmContext context, Revision from, Revision to) throws ScmException
    {
        PersistentContext persistentContext = context.getPersistentContext();
        persistentContext.tryLock(DEFAULT_TIMEOUT, SECONDS);
        try
        {
            List<GitLogEntry> entries = gitlog(context, from, to);

            // be aware, the log contains duplicate file entries for the edit/delete case.

            List<Changelist> changelists = new LinkedList<Changelist>();
            for (GitLogEntry entry : entries)
            {
                Revision rev = new Revision(entry.getId());

                List<FileChange> changes = new LinkedList<FileChange>();
                for (GitLogEntry.FileChangeEntry file : entry.getFiles())
                {
                    FileChange.Action action = FileChange.Action.UNKNOWN;
                    String actionString = file.getAction();
                    if (actionString.length() > 1)
                    {
                        // On merge the action has a character for each parent
                        // revision, and there are at least two of them.
                        action = FileChange.Action.MERGE;
                    }
                    else if (LOG_ACTION_MAPPINGS.containsKey(actionString))
                    {
                        action = LOG_ACTION_MAPPINGS.get(file.getAction());
                    }

                    FileChange change = new FileChange(file.getName(), new Revision(entry.getId()), action);
                    changes.add(change);
                }

                // For changelists we require a date -- which we try to ignore
                // when it is unparseable.
                if (entry.getDate() != null)
                {
                    Changelist changelist = new Changelist(rev, entry.getDate().getTime(), entry.getAuthor(), entry.getComment(), changes);
                    changelists.add(changelist);
                }
                else
                {
                    LOG.warning("Ignoring change '" + entry.getId() + "' which has unparseable date.");
                }
            }

            return changelists;
        }
        finally
        {
            persistentContext.unlock();
        }
    }

    /**
     * Run a native git log between the two revisions.  The order of the
     * revisions is not important.
     *
     * @param context   the scm context
     * @param from      one end of the git log range
     * @param to        the other end of the git log range
     * @return  list of entries between the specified revision range.
     * 
     * @throws ScmException on error
     */
    private List<GitLogEntry> gitlog(ScmContext context, Revision from, Revision to) throws ScmException
    {
        NativeGit git = preparePersistentDirectory(context);
        List<GitLogEntry> entries = git.log(getRevisionString(from), getRevisionString(to));

        // if revision to < from, then the base log request will return no results.  So
        // lets try it the other way around.
        if (entries.size() == 0)
        {
            entries = git.log(getRevisionString(to), getRevisionString(from));
            entries = newArrayList(reverse(entries));
        }
        return entries;
    }

    public List<ScmFile> browse(ScmContext context, String path, Revision revision) throws ScmException
    {
        PersistentContext persistentContext = context.getPersistentContext();
        persistentContext.tryLock(DEFAULT_TIMEOUT, SECONDS);
        try
        {
            NativeGit nativeGit = preparePersistentDirectory(context);
            String treeish = getRevisionString(revision);
            if (StringUtils.stringSet(path))
            {
                // We need to determine if it is a directory, in which case we
                // need to append a slash for git to list the contenst.  If we
                // get no output, the file doesn't exist.
                List<ScmFile> singleList = nativeGit.lsTree(treeish, path);
                if (singleList.isEmpty() || !singleList.get(0).isDirectory())
                {
                    return singleList;
                }

                path += "/";
            }

            return nativeGit.lsTree(treeish, path);
        }
        finally
        {
            persistentContext.unlock();
        }
    }

    public void tag(ScmContext scmContext, Revision revision, String name, boolean moveExisting) throws ScmException
    {
        PersistentContext persistentContext = scmContext.getPersistentContext();
        persistentContext.tryLock(DEFAULT_TIMEOUT, SECONDS);
        try
        {
            NativeGit nativeGit = new NativeGit(inactivityTimeout, scmContext.getEnvironmentContext());
            nativeGit.setWorkingDirectory(persistentContext.getPersistentWorkingDir());
            nativeGit.tag(revision, name, "[pulse] applying tag", moveExisting);
            nativeGit.push("origin", name);
        }
        finally
        {
            persistentContext.unlock();
        }
    }

    public Revision parseRevision(ScmContext context, String revision) throws ScmException
    {
        // FIXME this is not validating anything as it should.
        if (!StringUtils.stringSet(revision))
        {
            throw new ScmException("Unexpected git revision format: '" + revision + "'");
        }
        return new Revision(revision);
    }

    public Revision getPreviousRevision(ScmContext context, Revision fileRevision, boolean isFile) throws ScmException
    {
        // The previous revision is in fact revision^.  However, we should
        // return the actual SHA this is resolved as.
        return null;
    }

    public String getEmailAddress(ScmContext context, String user) throws ScmException
    {
        PersistentContext persistentContext = context.getPersistentContext();
        persistentContext.tryLock(DEFAULT_TIMEOUT, SECONDS);
        try
        {
            NativeGit git = preparePersistentDirectory(context);
            List<String> command = new LinkedList<String>();
            command.add(git.getGitCommand());
            command.add(COMMAND_LOG);
            command.add("-1");
            command.add(FLAG_AUTHOR);
            command.add("^" + user);
            command.add(FLAG_PRETTY + "=format:%ae");
            ScmOutputCapturingHandler handler = new ScmOutputCapturingHandler(Charset.defaultCharset());
            git.runWithHandler(handler, null, false, command.toArray(new String[command.size()]));

            String email = handler.getOutput();
            if (email != null)
            {
                email = email.trim();
                if (email.length() == 0)
                {
                    email = null;
                }
            }

            return email;
        }
        finally
        {
            persistentContext.unlock();
        }
    }

    public boolean configChangeRequiresClean(ScmConfiguration oldConfig, ScmConfiguration newConfig)
    {
        GitConfiguration oldGit = (GitConfiguration) oldConfig;
        GitConfiguration newGit = (GitConfiguration) newConfig;
        return !Objects.equal(oldGit.getRepository(), newGit.getRepository()) ||
                !Objects.equal(oldGit.getBranch(), newGit.getBranch()) ||
                oldGit.getCloneType() != newGit.getCloneType() ||
                oldGit.getCloneDepth() != newGit.getCloneDepth() ||
                oldGit.getSubmoduleProcessing() != newGit.getSubmoduleProcessing() ||
                !Objects.equal(oldGit.getSelectedSubmodules(), newGit.getSelectedSubmodules());
    }

    private String getRemoteBranchRef()
    {
        if (cloneType == GitConfiguration.CloneType.FULL_MIRROR)
        {
            return branch;
        }
        else
        {
            return REMOTE_ORIGIN + "/" + branch;
        }
    }

    private String getRevisionString(Revision revision)
    {
        if (revision == null)
        {
            return getRemoteBranchRef();
        }
        else
        {
            return revision.getRevisionString();
        }
    }
    
    public void setRepository(String repository)
    {
        this.repository = repository;
    }

    public void setBranch(String branch)
    {
        this.branch = branch;
    }

    public void setFilterPaths(List<String> includedPaths, List<String> excludedPaths)
    {
        this.includedPaths = includedPaths;
        this.excludedPaths = excludedPaths;
    }

    public void setCloneType(GitConfiguration.CloneType cloneType)
    {
        this.cloneType = cloneType;
    }

    public void setProcessSubmodules(boolean processSubmodules)
    {
        this.processSubmodules = processSubmodules;
    }

    public void setSubmoduleNames(List<String> submoduleNames)
    {
        this.submoduleNames = submoduleNames;
    }

    public void testConnection(ScmContext context) throws ScmException
    {
        NativeGit git = new NativeGit(inactivityTimeout, context.getEnvironmentContext());
        LsRemoteOutputHandler handler = new LsRemoteOutputHandler();
        git.lsRemote(handler, repository, branch);

        String stderr = handler.getStderr().trim();
        if (StringUtils.stringSet(stderr))
        {
            throw new GitException("Command '" + handler.getCommandLine() + "' output error: " + stderr);
        }

        if (!StringUtils.stringSet(handler.getStdout()))
        {
            throw new GitException("Branch '" + branch + "' does not exist");
        }
    }

    private static class LsRemoteOutputHandler implements ScmLineHandler
    {
        private String commandLine;
        private String stdout = "";
        private String stderr = "";

        public String getCommandLine()
        {
            return commandLine;
        }

        public String getStdout()
        {
            return stdout;
        }

        public String getStderr()
        {
            return stderr;
        }

        public void handleCommandLine(String line)
        {
            commandLine = line;
        }

        public void handleStdout(String line)
        {
            stdout += line + '\n';
        }

        public void handleStderr(String line)
        {
            stderr += line + '\n';
        }

        public void handleExitCode(int code) throws GitException
        {
            // Exit code checked in NativeGit
        }

        public void checkCancelled()
        {
        }
    }
}
