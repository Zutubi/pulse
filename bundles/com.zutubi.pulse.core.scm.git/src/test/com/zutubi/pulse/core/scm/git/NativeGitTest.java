package com.zutubi.pulse.core.scm.git;

import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.core.scm.ScmCancelledException;
import com.zutubi.pulse.core.scm.ScmEventHandler;
import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.ZipUtils;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.text.ParseException;

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

    public void testCloneStatusMessages() throws ScmException
    {
        final List<String> statusMessages = new LinkedList<String>();
        git.setWorkingDirectory(tmp);
        git.setScmEventHandler(new ScmEventHandlerAdapter()
        {
            public void status(String message)
            {
                statusMessages.add(message);
            }
        });
        git.clone(repository, "base");

        assertEquals(1, statusMessages.size());
        String message = statusMessages.get(0);
        assertTrue(message.startsWith("Initialized empty Git repository"));
    }

    public void testCloneCancelled() throws ScmException
    {
/* causing winslave to leave open file handled, need to investigate to ensure that the processes are being cleaned up.
        try
        {
            git.setWorkingDirectory(tmp);
            git.setScmEventHandler(new ScmEventHandlerAdapter()
            {
                public void checkCancelled() throws ScmCancelledException
                {
                    throw new ScmCancelledException("Operation cancelled");
                }
            });
            git.clone(repository, "base");
            fail("Expected the operation to be cancelled.");
        }
        catch (ScmException e)
        {
            // expected.
        }
*/
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

    public void testLogOnClone() throws ScmException, ParseException
    {
        git.setWorkingDirectory(tmp);
        git.clone(repository, "base");
        git.setWorkingDirectory(new File(tmp, "base"));

        List<GitLogEntry> entries = git.log("HEAD^", "HEAD");
        assertEquals(1, entries.size());
        GitLogEntry entry = entries.get(0);
        assertEquals("Daniel Ostermeier <daniel@zutubi.com>", entry.getAuthor());
        assertEquals("78be6b2f12399ea2332a5148440086913cb910fb", entry.getCommit());
        assertEquals("update", entry.getComment());
        assertEquals(new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy z").parse("Fri Sep 12 11:30:12 2008 EST"), entry.getDate());
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

    private class ScmEventHandlerAdapter implements ScmEventHandler
    {
        public void status(String message)
        {

        }

        public void fileChanged(Change change)
        {

        }

        public void checkCancelled() throws ScmCancelledException
        {

        }
    }

}
