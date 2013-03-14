package com.zutubi.pulse.core.scm.git;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import com.zutubi.pulse.core.scm.RecordingScmFeedbackHandler;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.core.util.PulseZipUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.List;

import static com.google.common.collect.Lists.transform;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class NativeGitTest extends PulseTestCase
{
    private File tmp;
    private NativeGit git;
    private String repository;
    private File repositoryBase;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = createTempDirectory();

        git = new NativeGit();

        URL url = getClass().getResource("NativeGitTest.zip");
        PulseZipUtils.extractZip(new File(url.toURI()), new File(tmp, "repo"));

        repositoryBase = new File(tmp, "repo");

        repository = "file://" + repositoryBase.getCanonicalPath();
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmp);

        super.tearDown();
    }

    public void testSystemPropertyPickedUp()
    {
        final String INVALID_COMMAND = "thereisnosuchcommand";

        String previousValue = System.getProperty(GitConstants.PROPERTY_GIT_COMMAND);
        System.setProperty(GitConstants.PROPERTY_GIT_COMMAND, INVALID_COMMAND);
        try
        {
            git.log();
            fail("Git should not run when a bad command is set");
        }
        catch (ScmException e)
        {
            assertTrue("Message '" + e.getMessage() + "' does not contain the invalid command", e.getMessage().contains(INVALID_COMMAND));
        }
        finally
        {
            if (previousValue == null)
            {
                System.clearProperty(GitConstants.PROPERTY_GIT_COMMAND);
            }
            else
            {
                System.setProperty(GitConstants.PROPERTY_GIT_COMMAND, previousValue);
            }
        }
    }

    public void testClone() throws ScmException, IOException
    {
        git.setWorkingDirectory(tmp);
        git.clone(new RecordingScmFeedbackHandler(), repository, "base", false, -1);

        File cloneBase = new File(tmp, "base");
        assertTrue(new File(cloneBase, ".git").isDirectory());

        // no content is checked out by default.
        assertFalse(new File(cloneBase, "README.txt").isFile());
        assertFalse(new File(cloneBase, "build.xml").isFile());
    }

    public void testCloneStatusMessages() throws ScmException
    {
        RecordingScmFeedbackHandler handler = new RecordingScmFeedbackHandler();
        git.setWorkingDirectory(tmp);
        git.clone(handler,  repository, "base", false, -1);

        assertThat(handler.getStatusMessages().size(), greaterThan(1));
        assertThat(handler.getStatusMessages(),
                   hasItem(anyOf(
                           startsWith("Cloning into base..."),
                           startsWith("Cloning into 'base'..."),
                           startsWith("Initialized empty Git repository"))));
    }

    public void testLog() throws ScmException
    {
        git.setWorkingDirectory(tmp);
        git.clone(null, repository, "base", false, -1);
        git.setWorkingDirectory(new File(tmp, "base"));

        assertEquals(2, git.log().size());
    }

    public void testLogHead() throws ScmException, ParseException
    {
        git.setWorkingDirectory(tmp);
        git.clone(null, repository, "base", false, -1);
        git.setWorkingDirectory(new File(tmp, "base"));

        List<GitLogEntry> entries = git.log("HEAD^", "HEAD");
        assertEquals(1, entries.size());
        GitLogEntry entry = entries.get(0);
        assertEquals("78be6b2f12399ea2332a5148440086913cb910fb", entry.getId());
    }

    public void testLogCount() throws ScmException, ParseException
    {
        git.setWorkingDirectory(tmp);
        git.clone(null, repository, "base", false, -1);
        git.setWorkingDirectory(new File(tmp, "base"));

        List<GitLogEntry> entries = git.log(1);
        assertEquals(1, entries.size());
        GitLogEntry entry = entries.get(0);
        assertEquals("78be6b2f12399ea2332a5148440086913cb910fb", entry.getId());
    }

    public void testBranchOnOriginalRepository() throws ScmException
    {
        git.setWorkingDirectory(repositoryBase);
        List<GitBranchEntry> branches = git.branch();

        assertNotNull(branches);
        assertEquals(2, branches.size());
        assertEquals("branch", branches.get(0).getName());
        assertEquals("master", branches.get(1).getName());
    }

    public void testBranchOnCloneRepository() throws ScmException
    {
        git.setWorkingDirectory(tmp);
        git.clone(null, repository, "base", false, -1);
        git.setWorkingDirectory(new File(tmp, "base"));

        List<GitBranchEntry> branches = git.branch();

        assertNotNull(branches);
        assertEquals(1, branches.size());
        assertEquals("master", branches.get(0).getName());
    }

    public void testCheckoutBranch() throws ScmException, IOException
    {
        git.setWorkingDirectory(tmp);
        git.clone(null, repository, "base", false, -1);

        File cloneBase = new File(tmp, "base");
        git.setWorkingDirectory(cloneBase);
        git.checkout(null, "master");

        assertFalse(Files.toString(new File(cloneBase, "README.txt"), Charset.defaultCharset()).contains("ON BRANCH"));

        git.checkout(null, "origin/branch", "local");

        assertTrue(Files.toString(new File(cloneBase, "README.txt"), Charset.defaultCharset()).contains("ON BRANCH"));
    }

    public void testLogParse() throws IOException, GitException
    {
        assertStandardLogEntries(parseLog());
    }

    public void testLogParseLeadingBlankLines() throws IOException, GitException
    {
        assertStandardLogEntries(parseLog());
    }

    public void testLogParseNoNewlineAfterBody() throws IOException, GitException
    {
        assertStandardLogEntries(parseLog());
    }

    public void testLogParseNonAsciiCharacters() throws IOException, GitException
    {
        List<GitLogEntry> logEntries = parseLog();

        assertEquals(7, logEntries.size());
        assertFalse(Iterables.any(logEntries, new Predicate<GitLogEntry>()
        {
            public boolean apply(GitLogEntry gitLogEntry)
            {
                return gitLogEntry.getDate() == null;
            }
        }));

        assertEquals("Dev3 (Name Lastname, lastname includes norwegian characters. Special norwegian characters are æ.ø,å)", logEntries.get(1).getAuthor());

        List<String> revisions = transform(logEntries, new Function<GitLogEntry, String>()
        {
            public String apply(GitLogEntry gitLogEntry)
            {
                return gitLogEntry.getId();
            }
        });
        assertEquals(asList(
                "d724b1708149ec8eac0d7f6ad44161d00c66c1b2",
                "ec13cf30f10dabce37af3ce9d6763066e8cf4cc4",
                "a12b1538df45268f2a66ec55856ffeeb131eb751",
                "b91f1a2f749586014e8b28c6766015ff6ba62ee7",
                "23572588c4b0c759c14a6c1687e5bd86461e31e8",
                "1a71e4b7960ba27fe6d47815da9337020085ac16",
                "391441b2ce2be527db0829798631436670df0965"
                ),
                revisions);
        }

        private List<GitLogEntry> parseLog() throws IOException, GitException
        {
            NativeGit.LogOutputHandler handler = new NativeGit.LogOutputHandler();
            BufferedReader reader = new BufferedReader(new InputStreamReader(getInput("txt")));
            String line;
            while ((line = reader.readLine()) != null)
            {
                handler.handleStdout(line);
            }

            return handler.getEntries();
        }

        private void assertStandardLogEntries(List<GitLogEntry> entries)
        {
            assertEquals(2, entries.size());

            GitLogEntry entry = entries.get(0);
            assertEquals("c3bb01f6713e2625d4cc70e6d689feabf88731b4", entry.getId());
            assertEquals("Alan User", entry.getAuthor());
            assertEquals("Revert \"Some change\"\nThis reverts commit dcfef78f9a8be49b71512d47e3bbe076c2beb4c8.", entry.getComment());
            assertEquals(1257227193000L, entry.getDate().getTime());
            assertSingleFileEntry(entry, "configure.exe");

            entry = entries.get(1);
            assertEquals("406d8ef682af938172aa5f196774666f83811289", entry.getId());
            assertEquals("Alan User", entry.getAuthor());
            assertEquals("Revert \"Another Change\"\n" +
                    "This reverts commit 0d29f576e6a64b61f56b3087fd72b2d21ca65f7f.", entry.getComment());
            assertEquals(1257327193000L, entry.getDate().getTime());
            assertSingleFileEntry(entry, "tools/configure/configureapp.cpp");
        }

    private void assertSingleFileEntry(GitLogEntry entry, String path)
    {
        List<GitLogEntry.FileChangeEntry> files;
        GitLogEntry.FileChangeEntry file;
        files = entry.getFiles();
        assertEquals(1, files.size());
        file = files.get(0);
        assertEquals("M", file.getAction());
        assertEquals(path, file.getName());
    }

    public void testPull() throws ScmException, IOException
    {
        git.setWorkingDirectory(tmp);
        git.clone(null, repository, "base", false, -1);

        git.setWorkingDirectory(new File(tmp, "base"));
        git.pull(null);
    }
}
