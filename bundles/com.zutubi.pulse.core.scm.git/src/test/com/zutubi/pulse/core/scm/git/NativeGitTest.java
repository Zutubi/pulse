package com.zutubi.pulse.core.scm.git;

import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.ZipUtils;
import com.zutubi.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
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

    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = FileSystemUtils.createTempDir();

        git = new NativeGit();

        URL url = getClass().getResource("repo.git.zip");
        ZipUtils.extractZip(new File(url.toURI()), new File(tmp, "repository"));

        File repoBase = new File(tmp, "repository");

        repository = "file://" + repoBase.getCanonicalPath();
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmp);

        super.tearDown();
    }

    public void testClone() throws ScmException
    {
        git.setWorkingDirectory(tmp);
        git.clone(repository, "base");

        File baseClone = new File(tmp, "base");
        assertTrue(new File(baseClone, ".git").isDirectory());
        assertTrue(new File(baseClone, "README.txt").isFile());
        assertTrue(new File(baseClone, "build.xml").isFile());
    }

    public void testPull() throws ScmException
    {
        git.setWorkingDirectory(tmp);
        git.clone(repository, "blah");

        git.setWorkingDirectory(new File(tmp, "blah"));
        git.pull();
    }

    public void testLog() throws ScmException, ParseException
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

    public void testCheckoutBranch() throws ScmException, IOException
    {
/*
        git.setWorkingDirectory(tmp);
        git.clone(repository, "base");

        File baseClone = new File(tmp, "base");
        File readmeFile = new File(baseClone, "README.txt");

        String fileContents = IOUtils.fileToString(readmeFile);
        assertFalse(fileContents.contains("ON BRANCH"));

        git.setWorkingDirectory(baseClone);
        git.checkout("BRANCH");

        fileContents = IOUtils.fileToString(readmeFile);
        assertTrue(fileContents.contains("ON BRANCH"));
*/
    }
}
