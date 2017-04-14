/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.core.scm.git;

import com.google.common.base.Predicate;
import static com.google.common.collect.Iterables.find;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.util.StringUtils;

import java.util.EnumSet;
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

    public String getRemoteRef(NativeGit git) throws ScmException
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
        GitBranchEntry activeBranch = find(branches, new Predicate<GitBranchEntry>()
        {
            public boolean apply(GitBranchEntry gitBranchEntry)
            {
                return gitBranchEntry.isActive();
            }
        }, null);

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

    public Revision update(WorkingCopyContext context, Revision revision) throws ScmException
    {
        throw new ScmException("Operation not supported.");
    }
}
