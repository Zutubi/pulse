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

import com.zutubi.pulse.core.scm.api.ScmException;
import static com.zutubi.pulse.core.scm.git.GitConstants.COMMAND_PULL;

import java.io.IOException;

public class GitWorkingCopyTest extends GitWorkingCopyTestBase
{
    public void testGetLatestRepositoryRevision() throws ScmException
    {
        assertEquals(REVISION_HEAD, workingCopy.getLatestRemoteRevision(context).getRevisionString());
    }

    public void testGetLatestRepositoryRevisionAfterLocalCommit() throws ScmException, IOException
    {
        editFile(baseDir, "file1");
        assertEquals(REVISION_HEAD, workingCopy.getLatestRemoteRevision(context).getRevisionString());
    }

    public void testGetLatestRepositoryRevisionAfterOtherPush() throws ScmException, IOException
    {
        String latest = otherPush();
        assertEquals(latest, workingCopy.getLatestRemoteRevision(context).getRevisionString());
    }

    public void testGetLatestRepositoryRevisionOnBranch() throws ScmException, IOException
    {
        switchToBranch(BRANCH_EXPERIMENTAL);
        assertEquals(REVISION_EXPERIMENTAL, workingCopy.getLatestRemoteRevision(context).getRevisionString());
    }

    public void testGuessLocalRevision() throws ScmException
    {
        assertEquals(REVISION_HEAD, workingCopy.guessLocalRevision(context).getRevisionString());
    }

    public void testGuessLocalRevisionAfterLocalCommit() throws ScmException, IOException
    {
        editFile(baseDir, "file1");
        assertEquals(REVISION_HEAD, workingCopy.guessLocalRevision(context).getRevisionString());
    }

    public void testGuessLocalRevisionAfterOtherPush() throws ScmException, IOException
    {
        otherPush();
        assertEquals(REVISION_HEAD, workingCopy.guessLocalRevision(context).getRevisionString());
    }

    public void testGuessLocalRevisionAfterOtherPushThenPull() throws ScmException, IOException
    {
        String latest = otherPush();
        runGit(baseDir, COMMAND_PULL);
        assertEquals(latest, workingCopy.guessLocalRevision(context).getRevisionString());
    }

    public void testGuessLocalRevisionOnBranch() throws ScmException, IOException
    {
        switchToBranch(BRANCH_EXPERIMENTAL);
        assertEquals(REVISION_EXPERIMENTAL, workingCopy.guessLocalRevision(context).getRevisionString());
    }
}
