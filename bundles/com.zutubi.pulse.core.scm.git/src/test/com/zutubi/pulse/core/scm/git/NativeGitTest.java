package com.zutubi.pulse.core.scm.git;

import com.zutubi.pulse.core.scm.RecordingScmEventHandler;
import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.ZipUtils;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class NativeGitTest extends PulseTestCase
{
    private File tmp;
    private NativeGit git;
    private String repository;
    private File repositoryBase;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = FileSystemUtils.createTempDir();

        git = new NativeGit();

        URL url = getClass().getResource("repo.git.zip");
        ZipUtils.extractZip(new File(url.toURI()), new File(tmp, "repo"));

        repositoryBase = new File(tmp, "repo");

        repository = "file://" + repositoryBase.getCanonicalPath();
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmp);

        super.tearDown();
    }

    public void testClone() throws ScmException, IOException
    {
        git.setScmEventHandler(new RecordingScmEventHandler());
        git.setWorkingDirectory(tmp);
        git.clone(repository, "base");

        File cloneBase = new File(tmp, "base");
        assertTrue(new File(cloneBase, ".git").isDirectory());

        // no content is checked out by default.
        assertFalse(new File(cloneBase, "README.txt").isFile());
        assertFalse(new File(cloneBase, "build.xml").isFile());
    }

    public void testCloneStatusMessages() throws ScmException
    {
        RecordingScmEventHandler handler = new RecordingScmEventHandler();
        git.setWorkingDirectory(tmp);
        git.setScmEventHandler(handler);
        git.clone(repository, "base");

        assertEquals(1, handler.getStatusMessages().size());
        String message = handler.getStatusMessages().get(0);
        assertTrue(message.startsWith("Initialized empty Git repository"));
    }

    public void testLog() throws ScmException
    {
        git.setWorkingDirectory(tmp);
        git.clone(repository, "base");
        git.setWorkingDirectory(new File(tmp, "base"));

        assertEquals(2, git.log().size());
    }

    public void testLogHead() throws ScmException, ParseException
    {
        git.setWorkingDirectory(tmp);
        git.clone(repository, "base");
        git.setWorkingDirectory(new File(tmp, "base"));

        List<GitLogEntry> entries = git.log("HEAD^", "HEAD");
        assertEquals(1, entries.size());
        GitLogEntry entry = entries.get(0);
        assertEquals("Daniel Ostermeier <daniel@zutubi.com>", entry.getAuthor());
        assertEquals("78be6b2f12399ea2332a5148440086913cb910fb", entry.getId());
        assertEquals("    update", entry.getComment());
        assertEquals(new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z").parse("Fri Sep 12 11:30:12 2008 +1000"), entry.getDate());
    }

    public void testLogCount() throws ScmException, ParseException
    {
        git.setWorkingDirectory(tmp);
        git.clone(repository, "base");
        git.setWorkingDirectory(new File(tmp, "base"));

        List<GitLogEntry> entries = git.log(1);
        assertEquals(1, entries.size());
        GitLogEntry entry = entries.get(0);
        assertEquals("Daniel Ostermeier <daniel@zutubi.com>", entry.getAuthor());
        assertEquals("78be6b2f12399ea2332a5148440086913cb910fb", entry.getId());
        assertEquals("    update", entry.getComment());
        assertEquals(new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z").parse("Fri Sep 12 11:30:12 2008 +1000"), entry.getDate());
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
        git.clone(repository, "base");
        git.setWorkingDirectory(new File(tmp, "base"));

        List<GitBranchEntry> branches = git.branch();

        assertNotNull(branches);
        assertEquals(1, branches.size());
        assertEquals("master", branches.get(0).getName());
    }

    public void testCheckoutBranch() throws ScmException, IOException
    {
        git.setWorkingDirectory(tmp);
        git.clone(repository, "base");

        File cloneBase = new File(tmp, "base");
        git.setWorkingDirectory(cloneBase);
        git.checkout("master");
        
        assertFalse(IOUtils.fileToString(new File(cloneBase, "README.txt")).contains("ON BRANCH"));
        
        git.checkout("origin/branch", "local");

        assertTrue(IOUtils.fileToString(new File(cloneBase, "README.txt")).contains("ON BRANCH"));
    }

    public void testPull() throws ScmException, IOException
    {
        git.setWorkingDirectory(tmp);
        git.clone(repository, "base");

        git.setWorkingDirectory(new File(tmp, "base"));
        git.pull();
    }
}
