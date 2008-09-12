package com.zutubi.pulse.core.scm.git;

import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.ZipUtils;
import com.zutubi.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.net.URL;

/**
 *
 *
 */
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
        git.setWorkingDirectory(tmp);
        git.clone(repository, "base");

        File cloneBase = new File(tmp, "base");
        assertTrue(new File(cloneBase, ".git").isDirectory());
        assertTrue(new File(cloneBase, "README.txt").isFile());
        assertTrue(new File(cloneBase, "build.xml").isFile());
    }

    public void testLogOnOriginalRepository() throws ScmException
    {
        git.setWorkingDirectory(repositoryBase);

        List<GitLogEntry> entries = git.log("HEAD^", "HEAD");
        assertEquals(1, entries.size());
        GitLogEntry entry = entries.get(0);
        assertNotNull(entry.getAuthor());
        assertNotNull(entry.getComment());
        assertNotNull(entry.getCommit());
        assertNotNull(entry.getDate());
    }

    public void testLogOnClone() throws ScmException
    {
        git.setWorkingDirectory(tmp);
        git.clone(repository, "base");
        git.setWorkingDirectory(new File(tmp, "base"));

        List<GitLogEntry> entries = git.log("HEAD^", "HEAD");
        assertEquals(1, entries.size());
        GitLogEntry entry = entries.get(0);
        assertNotNull(entry.getAuthor());
        assertNotNull(entry.getComment());
        assertNotNull(entry.getCommit());
        assertNotNull(entry.getDate());
    }

    public void testBranchOnOriginalRepository() throws ScmException
    {
        git.setWorkingDirectory(repositoryBase);
        List<GitBranchEntry> branches = git.branch();

        assertNotNull(branches);
        assertEquals(2, branches.size());
    }

    public void testBranchOnClone() throws ScmException
    {
        git.setWorkingDirectory(tmp);
        git.clone(repository, "base");
        git.setWorkingDirectory(new File(tmp, "base"));

        List<GitBranchEntry> branches = git.branch();

        assertNotNull(branches);
        assertEquals(1, branches.size());
    }

    public void testCheckoutBranch() throws ScmException, IOException
    {
        git.setWorkingDirectory(tmp);
        git.clone(repository, "base");

        File cloneBase = new File(tmp, "base");
        git.setWorkingDirectory(cloneBase);

        assertFalse(IOUtils.fileToString(new File(cloneBase, "README.txt")).contains("ON BRANCH"));

        git.fetch();

        git.checkout("branch");

        assertTrue(IOUtils.fileToString(new File(cloneBase, "README.txt")).contains("ON BRANCH"));
    }

    public void testPull() throws ScmException, IOException
    {
        git.setWorkingDirectory(tmp);
        git.clone(repository, "base");

        git.setWorkingDirectory(new File(tmp, "base"));
        git.pull();

        //TODO: need to verify that something new has been picked up.

        // update to revision x, then pull to HEAD.
    }

}
