package com.zutubi.pulse.core.scm.git;

import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.api.WorkingCopy;
import com.zutubi.pulse.core.scm.api.WorkingCopyContext;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.StringUtils;

import java.io.File;
import java.util.List;

/**
 * Implementation of {@link WorkingCopy} for git.
 */
public class GitWorkingCopy implements WorkingCopy
{
    public boolean matchesLocation(WorkingCopyContext context, String location) throws ScmException
    {
        return false;
    }

    public Revision getLatestRemoteRevision(WorkingCopyContext context) throws ScmException
    {
        // $ git ls-remote origin refs/heads/<branch>
        // 9f7eaea201b2f408d9effbf82f2731957e284adf	refs/heads/<branch>

        // First we need to determine the name of the remote and the branch
        // corresponding to our local branch.
        NativeGit git = new NativeGit(-1);
        git.setWorkingDirectory(context.getBase());

        String branch = getLocalBranch(git);
        String remote = getRemoteForBranch(git, branch);
        String remoteBranch = getRemoteTrackingBranch(git, branch);

        NativeGit.OutputCapturingHandler capturingHandler = new NativeGit.OutputCapturingHandler();
        git.lsRemote(capturingHandler, remote, "refs/heads/" + remoteBranch);

        String output = getSingleOutputLine(capturingHandler);
        String[] pieces = output.split("\\s+");
        return new Revision(pieces[0]);
    }

    public Revision guessHaveRevision(WorkingCopyContext context) throws ScmException
    {
        // $ git rev-parse <remote>/<remote branch>
        // 9f7eaea201b2f408d9effbf82f2731957e284adf
        NativeGit git = new NativeGit(-1);
        git.setWorkingDirectory(context.getBase());

        // Get the name of the remote and the remote branch.
        String branch = getLocalBranch(git);
        String remote = getRemoteForBranch(git, branch);
        String remoteBranch = getRemoteTrackingBranch(git, branch);

        return new Revision(git.revParse(remote + "/" + remoteBranch));
    }

    private String getLocalBranch(NativeGit git) throws ScmException
    {
        List<GitBranchEntry> branches = git.branch();
        GitBranchEntry activeBranch = CollectionUtils.find(branches, new Predicate<GitBranchEntry>()
        {
            public boolean satisfied(GitBranchEntry gitBranchEntry)
            {
                return gitBranchEntry.isActive();
            }
        });

        if (activeBranch == null)
        {
            throw new ScmException("Cannot determine active branch");
        }

        return activeBranch.getName();
    }

    private String getRemoteForBranch(NativeGit git, String branch) throws ScmException
    {
        return git.getSingleConfig("branch." + branch + ".remote", GitConstants.REMOTE_ORIGIN);
    }

    private String getRemoteTrackingBranch(NativeGit git, String branch) throws ScmException
    {
        String remoteMerge = git.getSingleConfig("branch." + branch + ".merge");
        if (remoteMerge == null)
        {
            // Guess a matching branch name.
            return branch;
        }
        else
        {
            // Strip refs/heads/
            String[] pieces = StringUtils.split(branch, '/');
            return pieces[pieces.length - 1];
        }
    }

    private String getSingleOutputLine(NativeGit.OutputCapturingHandler capturingHandler) throws ScmException
    {
        List<String> output = capturingHandler.getOutputLines();
        if (output.size() != 1)
        {
            throw new ScmException("Expecting single line of output got: " + output);
        }

        String line = output.get(0).trim();
        if (line.length() == 0)
        {
            throw new ScmException("Expected non-trivial output");
        }

        return line;
    }

    public boolean writePatchFile(WorkingCopyContext context, File patchFile, String... scope) throws ScmException
    {
        throw new RuntimeException("Not implemented");
    }

    public Revision update(WorkingCopyContext context, Revision revision) throws ScmException
    {
        return null;
    }
}
