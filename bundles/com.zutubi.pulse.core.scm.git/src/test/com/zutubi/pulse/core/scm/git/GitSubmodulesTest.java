package com.zutubi.pulse.core.scm.git;

import com.google.common.io.Files;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.util.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class GitSubmodulesTest extends GitClientTestBase
{
    private static final String SUBMODULE1_NAME = "sub1";
    private static final String SUBMODULE2_NAME = "sub2";
    private static final String TEXT_FILE_NAME = "a.txt";
    private static final String SUBMODULE_COMMAND_SNIPPET = "submodule update";

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        if (!SystemUtils.IS_WINDOWS)
        {
            NativeGit git = new NativeGit(0, null);

            git.setWorkingDirectory(repositoryBase);
            git.run(git.getGitCommand(), "reset", "--hard", "HEAD");
            git.run(git.getGitCommand(), "checkout", "master");
            git.run(git.getGitCommand(), "remote", "rm", "origin");
            git.run(git.getGitCommand(), "remote", "add", "origin", repositoryBase.getAbsolutePath());
            addSubmodule(git, SUBMODULE1_NAME);
            addSubmodule(git, SUBMODULE2_NAME);

            git.setWorkingDirectory(repositoryBase);
            git.run(git.getGitCommand(), "commit", "-m", "Added submodules.");
        }
    }

    private void addSubmodule(NativeGit git, String name) throws IOException, ScmException
    {
        File submoduleDir = new File(tmp, name);
        assertTrue(submoduleDir.mkdir());
        git.setWorkingDirectory(submoduleDir);
        git.init(null);
        File textFile = new File(submoduleDir, TEXT_FILE_NAME);
        Files.write("some text", textFile, Charset.defaultCharset());
        git.run(git.getGitCommand(), "add", textFile.getName());
        git.run(git.getGitCommand(), "commit", "-m", "Added a file");
        git.setWorkingDirectory(repositoryBase);
        String submodulePath = "../" + name;
        git.run(git.getGitCommand(), "submodule", "add", submodulePath, submoduleDir.getName());
    }

    public void testNoSubmoduleProcessing() throws ScmException
    {
        if (!SystemUtils.IS_WINDOWS)
        {
            client.setProcessSubmodules(false);
            client.checkout(context, null, handler);
            assertThat(handler.getStatusMessages(), not(hasItem(containsString(SUBMODULE_COMMAND_SNIPPET))));

            assertSubmoduleNotUpdated(SUBMODULE1_NAME);
            assertSubmoduleNotUpdated(SUBMODULE2_NAME);
        }
    }

    public void testAllSubmoduleProcessing() throws ScmException
    {
        if (!SystemUtils.IS_WINDOWS)
        {
            client.setProcessSubmodules(true);
            client.checkout(context, null, handler);
            assertThat(handler.getStatusMessages(), hasItem(containsString(SUBMODULE_COMMAND_SNIPPET)));

            assertSubmoduleUpdated(SUBMODULE1_NAME);
            assertSubmoduleUpdated(SUBMODULE2_NAME);
        }
    }

    public void testSelectedSubmoduleProcessing() throws ScmException
    {
        if (!SystemUtils.IS_WINDOWS)
        {
            client.setProcessSubmodules(true);
            client.setSubmoduleNames(asList(SUBMODULE1_NAME));
            client.checkout(context, null, handler);
            assertThat(handler.getStatusMessages(), hasItem(containsString(SUBMODULE_COMMAND_SNIPPET)));

            assertSubmoduleUpdated(SUBMODULE1_NAME);
            assertSubmoduleNotUpdated(SUBMODULE2_NAME);
        }
    }

    private void assertSubmoduleNotUpdated(String name)
    {
        // Submodule dir will exist but will be empty.
        File submoduleDir = new File(workingDir, name);
        assertTrue(submoduleDir.isDirectory());
        File textFile = new File(submoduleDir, TEXT_FILE_NAME);
        assertFalse(textFile.isFile());
    }

    private void assertSubmoduleUpdated(String name)
    {
        // Submodule dir should be populated.
        File submoduleDir = new File(workingDir, name);
        assertTrue(submoduleDir.isDirectory());
        File textFile = new File(submoduleDir, TEXT_FILE_NAME);
        assertTrue(textFile.isFile());
    }
}
