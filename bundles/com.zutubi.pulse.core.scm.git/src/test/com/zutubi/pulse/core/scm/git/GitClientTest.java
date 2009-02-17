package com.zutubi.pulse.core.scm.git;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.scm.RecordingScmFeedbackHandler;
import com.zutubi.pulse.core.scm.ScmContextImpl;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.core.util.ZipUtils;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.Sort;
import com.zutubi.util.io.IOUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GitClientTest extends PulseTestCase
{
    private File tmp;
    private GitClient client;
    private File workingDir;
    private PulseExecutionContext context;
    private RecordingScmFeedbackHandler handler;
    private ScmContextImpl scmContext;
    
    private static final String TEST_AUTHOR = "Daniel Ostermeier";

    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = FileSystemUtils.createTempDir();

        URL url = getClass().getResource("GitClientTest.zip");
        ZipUtils.extractZip(new File(url.toURI()), new File(tmp, "repo"));

        File repositoryBase = new File(tmp, "repo");

        String repository = "file://" + repositoryBase.getCanonicalPath();

        client = new GitClient(repository, "master");

        workingDir = new File(tmp, "wd");
        context = new PulseExecutionContext();
        context.setWorkingDir(workingDir);

        scmContext = new ScmContextImpl();
        scmContext.setPersistentWorkingDir(workingDir);

        handler = new RecordingScmFeedbackHandler();
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmp);

        super.tearDown();
    }

    public void testCheckout() throws ScmException, ParseException
    {
        Revision rev = client.checkout(context, null, handler);

        assertEquals("e34da05e88de03a4aa5b10b338382f09bbe65d4b", rev.getRevisionString());

        assertFiles(workingDir, "a.txt", "b.txt", "c.txt");
        assertGitDir(workingDir);

        assertThat(handler.getStatusMessages().size(), greaterThan(0));
    }

    public void testCheckoutOnBranch() throws ScmException, ParseException
    {
        client.setBranch("branch");
        Revision rev = client.checkout(context, null, handler);

        assertEquals("c34b545b6954b8946967c250dde7617c24a9bb4b", rev.getRevisionString());

        assertFiles(workingDir, "1.txt", "2.txt", "3.txt");
        assertGitDir(workingDir);
    }

    public void testCheckoutToRevision() throws ScmException, ParseException
    {
        Revision rev = client.checkout(context, new Revision("96e8d45dd7627d9e3cab980e90948e3ae1c99c62"), handler);

        assertEquals("96e8d45dd7627d9e3cab980e90948e3ae1c99c62", rev.getRevisionString());

        assertFiles(workingDir, "a.txt", "b.txt", "c.txt");
        assertGitDir(workingDir);

        assertThat(handler.getStatusMessages().size(), greaterThan(0));
    }

    public void testCheckoutToRevisionOnBranch() throws ScmException, ParseException
    {
        client.setBranch("branch");
        Revision rev = client.checkout(context, new Revision("83d35b25a6b4711c4d9424c337bf82e5398756f3"), handler);

        assertEquals("83d35b25a6b4711c4d9424c337bf82e5398756f3", rev.getRevisionString());

        assertGitDir(workingDir);
        assertEquals(1, workingDir.list().length);
    }

    public void testGetLatestRevision() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        Revision rev = client.getLatestRevision(scmContext);

        assertEquals("e34da05e88de03a4aa5b10b338382f09bbe65d4b", rev.getRevisionString());
    }

    public void testGetLatestRevisionOnBranch() throws ScmException
    {
        client.setBranch("branch");
        client.init(scmContext, new ScmFeedbackAdapter());
        Revision rev = client.getLatestRevision(scmContext);

        assertEquals("c34b545b6954b8946967c250dde7617c24a9bb4b", rev.getRevisionString());
    }

    public void testRetrieve() throws ScmException, IOException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        InputStream content = client.retrieve(scmContext, "a.txt", null);
        assertEquals("", IOUtils.inputStreamToString(content));
    }

    public void testRetrieveFromRevision() throws IOException, ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        InputStream content = client.retrieve(scmContext, "a.txt", new Revision("b69a48a6b0f567d0be110c1fbca2c48fc3e1b112"));
        assertEquals("content", IOUtils.inputStreamToString(content));
    }

    public void testRetrieveOnBranch() throws ScmException, IOException
    {
        client.setBranch("branch");
        client.init(scmContext, new ScmFeedbackAdapter());
        InputStream content = client.retrieve(scmContext, "1.txt", null);
        assertEquals("", IOUtils.inputStreamToString(content));
    }

    public void testRetrieveFromRevisionOnBranch() throws ScmException, IOException
    {
        client.setBranch("branch");
        client.init(scmContext, new ScmFeedbackAdapter());
        InputStream content = client.retrieve(scmContext, "1.txt", new Revision("7d61890eb55586ec99416c53c581bf561591a608"));
        assertEquals("content", IOUtils.inputStreamToString(content));
    }

    public void testUpdate() throws ScmException
    {
        client.checkout(context, null, handler);
        assertThat(handler.getStatusMessages().size(), greaterThan(0));
        assertTrue(handler.getStatusMessages().contains("Branch local set up to track remote branch refs/remotes/origin/master."));

        handler.reset();

        client.update(context, null, handler);
        assertThat(handler.getStatusMessages().size(), greaterThan(0));
        assertEquals("Already up-to-date.", handler.getStatusMessages().get(handler.getStatusMessages().size() - 1));
    }

    public void testUpdateOnBranch() throws ScmException
    {
        client.setBranch("branch");
        client.checkout(context, null, handler);
        assertThat(handler.getStatusMessages().size(), greaterThan(0));
        assertTrue(handler.getStatusMessages().contains("Branch local set up to track remote branch refs/remotes/origin/branch."));

        handler.reset();

        client.update(context, null, handler);
        assertThat(handler.getStatusMessages().size(), greaterThan(0));
        assertEquals("Already up-to-date.", handler.getStatusMessages().get(handler.getStatusMessages().size() - 1));
    }

    public void testUpdateToRevision() throws ScmException, IOException, ParseException
    {
        client.checkout(context, null, handler);
        assertEquals("", IOUtils.fileToString(new File(workingDir, "a.txt")));

        Revision rev = client.update(context, new Revision("b69a48a6b0f567d0be110c1fbca2c48fc3e1b112"), handler);
        assertEquals("b69a48a6b0f567d0be110c1fbca2c48fc3e1b112", rev.getRevisionString());

        assertEquals("content", IOUtils.fileToString(new File(workingDir, "a.txt")));
    }

    public void testChanges() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        List<Changelist> changes = client.getChanges(scmContext, new Revision("HEAD~2"), null);
        assertEquals(2, changes.size());
    }

    public void testHeadChanges() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());

        List<Changelist> changes = client.getChanges(scmContext, new Revision("HEAD~1"), new Revision("HEAD"));
        assertEquals(1, changes.size());
        Changelist changelist = changes.get(0);
        assertEquals("removed content from a.txt", changelist.getComment());
        assertEquals("e34da05e88de03a4aa5b10b338382f09bbe65d4b", changelist.getRevision().getRevisionString());
        assertEquals(TEST_AUTHOR, changelist.getAuthor());
        assertEquals(1, changelist.getChanges().size());
        FileChange change = changelist.getChanges().get(0);
        assertEquals(FileChange.Action.EDIT, change.getAction());
        assertEquals("a.txt", change.getPath());
    }

    public void testBrowse() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());

        List<ScmFile> files = client.browse(scmContext, "", null);
        assertEquals(3, files.size());
        Collections.sort(files, new ScmFileComparator());
        assertEquals("a.txt", files.get(0).getName());
        assertEquals("b.txt", files.get(1).getName());
        assertEquals("c.txt", files.get(2).getName());
    }

    public void testBrowseNotAvailableWhenContextNotAvailable()
    {
        assertFalse(client.getCapabilities(null).contains(ScmCapability.BROWSE));
    }

    public void testBrowseAvailableWhenContextAvailable()
    {
        assertTrue(client.getCapabilities(scmContext).contains(ScmCapability.BROWSE));
    }

    private void assertFiles(File base, String... filenames)
    {
        for (String filename : filenames)
        {
            assertTrue(filename + " is not a file.", new File(base, filename).isFile());
        }
    }

    private void assertGitDir(File workingDir)
    {
        assertTrue(new File(workingDir, GitClient.GIT_REPOSITORY_DIRECTORY).isDirectory());
    }

    private static class ScmFileComparator implements Comparator<ScmFile>
    {
        private final Sort.StringComparator comp = new Sort.StringComparator();

        public int compare(ScmFile o1, ScmFile o2)
        {
            return comp.compare(o1.getName(), o2.getName());
        }
    }
}
