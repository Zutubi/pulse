package com.zutubi.pulse.core.scm.git;

import com.zutubi.pulse.core.scm.api.*;
import static com.zutubi.pulse.core.scm.git.GitConstants.*;
import com.zutubi.pulse.core.util.process.AsyncProcess;
import com.zutubi.pulse.core.util.process.ForwardingCharHandler;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.StringUtils;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.io.Writer;
import static java.util.Arrays.asList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Implementation of {@link WorkingCopy} for git.
 */
public class GitWorkingCopy implements WorkingCopy
{
    public Set<WorkingCopyCapability> getCapabilities()
    {
        return EnumSet.complementOf(EnumSet.of(WorkingCopyCapability.UPDATE));
    }

    public boolean matchesLocation(WorkingCopyContext context, String location) throws ScmException
    {
        return true;
    }

    public Revision getLatestRemoteRevision(WorkingCopyContext context) throws ScmException
    {
        // $ git ls-remote origin refs/heads/<branch>
        // 9f7eaea201b2f408d9effbf82f2731957e284adf	refs/heads/<branch>

        // First we need to determine the name of the remote and the branch
        // corresponding to our local branch.
        NativeGit git = new NativeGit();
        git.setWorkingDirectory(context.getBase());

        String branch = getLocalBranch(git);
        String remote = getRemoteForBranch(git, branch);
        String remoteBranch = getRemoteTrackingBranch(git, branch);

        NativeGit.OutputCapturingHandler capturingHandler = new NativeGit.OutputCapturingHandler();
        git.lsRemote(capturingHandler, remote, "refs/heads/" + remoteBranch);

        String output = capturingHandler.getSingleOutputLine();
        String[] pieces = output.split("\\s+");
        return new Revision(pieces[0]);
    }

    public Revision guessLocalRevision(WorkingCopyContext context) throws ScmException
    {
        // $ git rev-parse <remote>/<remote branch>
        // 9f7eaea201b2f408d9effbf82f2731957e284adf
        NativeGit git = new NativeGit();
        git.setWorkingDirectory(context.getBase());

        return new Revision(git.revisionParse(getRemoteRef(git)));
    }

    private String getRemoteRef(NativeGit git) throws ScmException
    {
        // Get the name of the remote and the remote branch.
        String branch = getLocalBranch(git);
        String remote = getRemoteForBranch(git, branch);
        String remoteBranch = getRemoteTrackingBranch(git, branch);
        return remote + "/" + remoteBranch;
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

    public boolean writePatchFile(WorkingCopyContext context, File patchFile, String... scope) throws ScmException
    {
        // Scope is of the form:
        //   [:<range>] <file> ...
        // where the optional <range> itself is of the form:
        //   [<commit>[..[.]<commit>]]
        // and is either empty (in which case we pass --cached), a single
        // commit (passed through to diff) or a commit range (passed through).
        //
        // If no commit range is present, we use <remote>/<remote branch>.  We
        // always add "--" after the commit range to avoid ambiguities.
        NativeGit git = new NativeGit();
        git.setWorkingDirectory(context.getBase());

        List<String> args = new LinkedList<String>();
        args.add(git.getGitCommand());
        args.add(COMMAND_DIFF);
        args.add(FLAG_BINARY);
        args.add(FLAG_FIND_COPIES);

        if (scope.length > 0 && scope[0].startsWith(":"))
        {
            String range = scope[0].substring(1);
            if (range.isEmpty())
            {
                args.add(FLAG_CACHED);
            }
            else
            {
                args.add(range);
            }

            if (scope.length > 1)
            {
                args.add(FLAG_SEPARATOR);
                args.addAll(asList(scope).subList(1, scope.length));
            }
        }
        else
        {
            args.add(getRemoteRef(git));
            if (scope.length > 0)
            {
                args.add(FLAG_SEPARATOR);
                args.addAll(asList(scope));
            }
        }

        // Run the process directly so we can capture raw output.  Going
        // through a line handler munges newlines.
        AsyncProcess async = null;
        Writer output = null;
        StringWriter error = new StringWriter();
        try
        {
            output = new FileWriter(patchFile);

            ProcessBuilder builder = new ProcessBuilder(args);
            builder.directory(context.getBase());
            Process p = builder.start();

            async = new AsyncProcess(p, new ForwardingCharHandler(output, error), true);
            int exitCode = async.waitFor();
            if (exitCode != 0)
            {
                context.getUI().error(error.toString());
                context.getUI().error("git diff exited with code " + exitCode + ".");
                return false;
            }
        }
        catch (Exception e)
        {
            context.getUI().error("Error writing patch file: " + e.getMessage(), e);
            return false;
        }
        finally
        {
            IOUtils.close(output);
            if (async != null)
            {
                async.destroy();
            }
        }

        if (patchFile.length() == 0)
        {
            context.getUI().status("No changes found.");
            if (!patchFile.delete())
            {
                throw new GitException("Can't remove empty patch '" + patchFile.getAbsolutePath() + "'");
            }

            return false;
        }

        return true;
    }

    public Revision update(WorkingCopyContext context, Revision revision) throws ScmException
    {
        throw new ScmException("Operation not supported.");
    }
}
