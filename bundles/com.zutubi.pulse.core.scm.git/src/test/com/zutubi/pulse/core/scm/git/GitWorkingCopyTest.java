package com.zutubi.pulse.core.scm.git;

import com.zutubi.pulse.core.personal.TestPersonalBuildUI;
import com.zutubi.pulse.core.scm.WorkingCopyContextImpl;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.api.WorkingCopyContext;
import static com.zutubi.pulse.core.scm.git.GitConstants.*;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.config.PropertiesConfig;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class GitWorkingCopyTest extends PulseTestCase
{
    private static final String REVISION_HEAD = "a69824696c9c9fa9383551b4a9a97653aed87483";
    private static final String REVISION_EXPERIMENTAL = "9ac3ca040cf09a8979201ab37378c45ec7409180";

    private static final String BRANCH_EXPERIMENTAL = "experimental";

    private File tempDir;
    private File baseDir;
    private File otherDir;
    private WorkingCopyContext context;
    private GitWorkingCopy workingCopy;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        tempDir = FileSystemUtils.createTempDir(getName(), ".tmp");

        File upstreamDir = new File(tempDir, "upstream");
        unzipInput("repo", upstreamDir);

        baseDir = new File(tempDir, "base");
        runGit(tempDir, COMMAND_CLONE, upstreamDir.getAbsolutePath(), "base");

        otherDir = new File(tempDir, "other");
        runGit(tempDir, COMMAND_CLONE, upstreamDir.getAbsolutePath(), "other");

        context = new WorkingCopyContextImpl(baseDir, new PropertiesConfig(), new TestPersonalBuildUI());
        workingCopy = new GitWorkingCopy();
    }

    @Override
    protected void tearDown() throws Exception
    {
        FileSystemUtils.rmdir(tempDir);
        super.tearDown();
    }

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

    public void testGuessHaveRevision() throws ScmException
    {
        assertEquals(REVISION_HEAD, workingCopy.guessHaveRevision(context).getRevisionString());
    }

    public void testGuessHaveRevisionAfterLocalCommit() throws ScmException, IOException
    {
        editFile(baseDir, "file1");
        assertEquals(REVISION_HEAD, workingCopy.guessHaveRevision(context).getRevisionString());
    }

    public void testGuessHaveRevisionAfterOtherPush() throws ScmException, IOException
    {
        otherPush();
        assertEquals(REVISION_HEAD, workingCopy.guessHaveRevision(context).getRevisionString());
    }

    public void testGuessHaveRevisionAfterOtherPushThenPull() throws ScmException, IOException
    {
        String latest = otherPush();
        runGit(baseDir, COMMAND_PULL);
        assertEquals(latest, workingCopy.guessHaveRevision(context).getRevisionString());
    }

    public void testGuessHaveRevisionOnBranch() throws ScmException, IOException
    {
        switchToBranch(BRANCH_EXPERIMENTAL);
        assertEquals(REVISION_EXPERIMENTAL, workingCopy.guessHaveRevision(context).getRevisionString());
    }

    private void switchToBranch(String branch) throws IOException
    {
        runGit(baseDir, COMMAND_CHECKOUT, FLAG_BRANCH, branch);
    }

    /**
     * Pushes a commit from the other clone and return the new latest revision.
     *
     * @return the new latest revision
     * @throws IOException on error editing a file
     * @throws GitException on git error
     */
    private String otherPush() throws IOException, GitException
    {

        editFile(otherDir, "file1");
        runGit(otherDir, COMMAND_PUSH);
        NativeGit nativeGit = new NativeGit(-1);
        nativeGit.setWorkingDirectory(otherDir);
        return nativeGit.log(1).get(0).getId();
    }


    private void editFile(File dir, String path) throws IOException
    {
        File f = new File(dir, path);
        FileSystemUtils.createFile(f, "edited in " + getName());
        runGit(dir, COMMAND_COMMIT, FLAG_ALL, FLAG_MESSAGE, "made an edit");
    }

    private String runGit(File workingDir, String... command) throws IOException
    {
        List<String> commands = new LinkedList<String>();
        commands.add("git");
        commands.addAll(Arrays.asList(command));

        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.directory(workingDir);
        return SystemUtils.runCommandWithInput(0, null, builder);
    }
}

// $ git log --name-status
//    commit a69824696c9c9fa9383551b4a9a97653aed87483
//    Author: Jason Sankey <jason@zutubi.com>
//    Date:   Mon Jun 22 15:38:57 2009 +0100
//
//        Another edit on master.
//
//    M	file1
//
//    commit 74d50a8ada25d15a2f829530d7580f0e4ae826bc
//    Author: Jason Sankey <jason@zutubi.com>
//    Date:   Mon Jun 22 15:33:29 2009 +0100
//
//        Changes on local branch.
//
//    M	dir1/nested
//    A	file3
//
//    commit 0cd9762c8e265acb548d6a667b49489a018a4bb9
//    Author: Jason Sankey <jason@zutubi.com>
//    Date:   Mon Jun 22 15:32:02 2009 +0100
//
//        Edit on master.
//
//    M	file1
//
//    commit 70017983e5eb11682398ca1cf5784cad5c8d5f5b
//    Author: Jason Sankey <jason@zutubi.com>
//    Date:   Mon Jun 22 15:30:50 2009 +0100
//
//        Initial import.
//
//    A	dir1/nested
//    A	file1
//    A	file2

// $ git log --name-status experimental
//    commit 9ac3ca040cf09a8979201ab37378c45ec7409180
//    Author: Jason Sankey <jason@zutubi.com>
//    Date:   Mon Jun 22 15:38:01 2009 +0100
//
//        Edits on experimental branch.
//
//    M	file2
//    D	file3
//
//    commit 74d50a8ada25d15a2f829530d7580f0e4ae826bc
//    Author: Jason Sankey <jason@zutubi.com>
//    Date:   Mon Jun 22 15:33:29 2009 +0100
//
//        Changes on local branch.
//
//    M	dir1/nested
//    A	file3
//
//    commit 0cd9762c8e265acb548d6a667b49489a018a4bb9
//    Author: Jason Sankey <jason@zutubi.com>
//    Date:   Mon Jun 22 15:32:02 2009 +0100
//
//        Edit on master.
//
//    M	file1
//
//    commit 70017983e5eb11682398ca1cf5784cad5c8d5f5b
//    Author: Jason Sankey <jason@zutubi.com>
//    Date:   Mon Jun 22 15:30:50 2009 +0100
//
//        Initial import.
//
//    A	dir1/nested
//    A	file1
//    A	file2
