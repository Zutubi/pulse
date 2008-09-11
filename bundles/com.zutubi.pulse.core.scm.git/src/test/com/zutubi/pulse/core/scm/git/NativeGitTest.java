package com.zutubi.pulse.core.scm.git;

import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 *
 *
 */
public class NativeGitTest extends PulseTestCase
{
    private File tmp;
    private NativeGit git;
    private String repository;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = FileSystemUtils.createTempDir();

/*
        git = new NativeGit();

        repository = "file:///c/tmp/git-testing/repo";
*/
//        repository = new File("/c/tmp/git-testing/check");
/*
        repository = new File(tmp, "repository");

        URL url = getClass().getResource("repo.git.zip");
        ZipUtils.extractZip(new File(url.toURI()), repository);
*/
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmp);

        super.tearDown();
    }

    public void testClone() throws ScmException, IOException
    {
/*
        git.setWorkingDirectory(tmp);
        git.clone("file:///c/tmp/git-testing/repo", "base");

        File cloneBase = new File(tmp, "base");
        assertTrue(new File(cloneBase, ".git").isDirectory());
        assertTrue(new File(cloneBase, "README.txt").isFile());
        assertTrue(new File(cloneBase, "build.xml").isFile());
*/
    }

    public void testFetchBranch() throws ScmException, IOException
    {
/*
        git.setWorkingDirectory(tmp);
        git.clone(repository, "base");

        File cloneBase = new File(tmp, "base");
        git.setWorkingDirectory(cloneBase);

        File readmeFile = new File(cloneBase, "README.txt");

        String fileContents = IOUtils.fileToString(readmeFile);
        assertFalse(fileContents.contains("ON BRANCH"));

        git.fetch("BRANCH");

        fileContents = IOUtils.fileToString(readmeFile);
        assertTrue(fileContents.contains("ON BRANCH"));
*/
    }

    public void testPull() throws ScmException, IOException
    {
/*
        git.setWorkingDirectory(tmp);
        git.clone(repository, "blah");

        git.setWorkingDirectory(new File(tmp, "blah"));
        git.pull();

        //TODO: need to verify that something new has been picked up.
*/
    }

    public void testLog() throws ScmException, ParseException, IOException
    {
/*
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
*/
    }

    public void testBranch() throws ScmException
    {
/*
        git.setWorkingDirectory(repository);
        List<GitBranchEntry> branches = git.branch();

        assertNotNull(branches);
        assertEquals(2, branches.size());
*/
    }
}
