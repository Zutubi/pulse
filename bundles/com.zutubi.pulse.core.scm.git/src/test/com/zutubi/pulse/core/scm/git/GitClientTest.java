package com.zutubi.pulse.core.scm.git;

import com.zutubi.pulse.core.ExecutionContext;
import com.zutubi.pulse.core.scm.RecordingScmFeedbackHandler;
import com.zutubi.pulse.core.scm.ScmContextImpl;
import com.zutubi.pulse.core.scm.api.Changelist;
import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.pulse.core.util.ZipUtils;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GitClientTest extends PulseTestCase
{
    private File tmp;
    private String repository;
    private GitClient client;
    private File workingDir;
    private ExecutionContext context;
    private RecordingScmFeedbackHandler handler;
    private ScmContextImpl scmContext;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = FileSystemUtils.createTempDir();

        URL url = getClass().getResource("GitClientTest.zip");
        ZipUtils.extractZip(new File(url.toURI()), new File(tmp, "repo"));

        File repositoryBase = new File(tmp, "repo");

        repository = "file://" + repositoryBase.getCanonicalPath();
//        repository = "file://c:\\tmp\\git-testing";

        client = new GitClient();
        client.setRepository(repository);
        client.setBranch("master");

        workingDir = new File(tmp, "wd");
        context = new ExecutionContext();
        context.setWorkingDir(workingDir);

        scmContext = new ScmContextImpl();
        scmContext.setPersistentWorkingDir(workingDir);

        handler = new RecordingScmFeedbackHandler();
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmp);
        tmp = null;
        client = null;
        repository = null;
        handler = null;
        workingDir = null;
        context = null;
        scmContext = null;

        super.tearDown();
    }

    public void testCheckout() throws ScmException, ParseException
    {
        Revision rev = client.checkout(context, null, handler);

        assertEquals("e34da05e88de03a4aa5b10b338382f09bbe65d4b", rev.getRevisionString());

        assertTrue(new File(workingDir, "a.txt").isFile());
        assertTrue(new File(workingDir, "b.txt").isFile());
        assertTrue(new File(workingDir, "c.txt").isFile());
        assertTrue(new File(workingDir, ".git").isDirectory());

        assertEquals(2, handler.getStatusMessages().size());
    }

    public void testCheckoutOnBranch() throws ScmException, ParseException
    {
        client.setBranch("branch");
        Revision rev = client.checkout(context, null, handler);

        assertEquals("c34b545b6954b8946967c250dde7617c24a9bb4b", rev.getRevisionString());

        assertTrue(new File(workingDir, "1.txt").isFile());
        assertTrue(new File(workingDir, "2.txt").isFile());
        assertTrue(new File(workingDir, "3.txt").isFile());
        assertTrue(new File(workingDir, ".git").isDirectory());
    }

    public void testCheckoutToRevision() throws ScmException, ParseException
    {
        Revision rev = client.checkout(context, new Revision("96e8d45dd7627d9e3cab980e90948e3ae1c99c62"), handler);

        assertEquals("96e8d45dd7627d9e3cab980e90948e3ae1c99c62", rev.getRevisionString());

        assertTrue(new File(workingDir, "a.txt").isFile());
        assertTrue(new File(workingDir, "b.txt").isFile());
        assertTrue(new File(workingDir, "c.txt").isFile());
        assertTrue(new File(workingDir, ".git").isDirectory());

        assertEquals(2, handler.getStatusMessages().size());
    }

    public void testCheckoutToRevisionOnBranch() throws ScmException, ParseException
    {
        client.setBranch("branch");
        Revision rev = client.checkout(context, new Revision("83d35b25a6b4711c4d9424c337bf82e5398756f3"), handler);

        assertEquals("83d35b25a6b4711c4d9424c337bf82e5398756f3", rev.getRevisionString());

        assertTrue(new File(workingDir, ".git").isDirectory());
        assertEquals(1, workingDir.list().length);
    }

    public void testGetLatestRevision() throws ScmException
    {
        Revision rev = client.getLatestRevision(scmContext);

        assertEquals("e34da05e88de03a4aa5b10b338382f09bbe65d4b", rev.getRevisionString());
    }

    public void testGetLatestRevisionOnBranch() throws ScmException
    {
        client.setBranch("branch");
        Revision rev = client.getLatestRevision(scmContext);

        assertEquals("c34b545b6954b8946967c250dde7617c24a9bb4b", rev.getRevisionString());
    }

    public void testRetrieve() throws ScmException, IOException
    {
        InputStream content = client.retrieve(scmContext, "a.txt", null);
        String data = readToString(content);
        assertEquals("", data);
    }

    public void testRetrieveFromRevision() throws IOException, ScmException
    {
        InputStream content = client.retrieve(scmContext, "a.txt", new Revision("b69a48a6b0f567d0be110c1fbca2c48fc3e1b112"));
        String data = readToString(content);
        assertEquals("content", data);
    }

    public void testRetrieveOnBranch() throws ScmException, IOException
    {
        client.setBranch("branch");
        InputStream content = client.retrieve(scmContext, "1.txt", null);
        String data = readToString(content);
        assertEquals("", data);
    }

    public void testRetrieveFromRevisionOnBranch() throws ScmException, IOException
    {
        client.setBranch("branch");
        InputStream content = client.retrieve(scmContext, "1.txt", new Revision("7d61890eb55586ec99416c53c581bf561591a608"));
        String data = readToString(content);
        assertEquals("content", data);
    }

    public void testUpdate() throws ScmException
    {
        client.setBranch("master");
        client.checkout(context, null, handler);
        assertEquals(2, handler.getStatusMessages().size());
        assertEquals("Branch local set up to track remote branch refs/remotes/origin/master.", handler.getStatusMessages().get(1));

        client.update(context, null, handler);
        assertEquals(3, handler.getStatusMessages().size());
        assertEquals("Already up-to-date.", handler.getStatusMessages().get(2));
    }

    public void testUpdateOnBranch() throws ScmException
    {
        client.setBranch("branch");
        client.checkout(context, null, handler);
        assertEquals(2, handler.getStatusMessages().size());
        assertEquals("Branch local set up to track remote branch refs/remotes/origin/branch.", handler.getStatusMessages().get(1));

        client.update(context, null, handler);
        assertEquals(3, handler.getStatusMessages().size());
        assertEquals("Already up-to-date.", handler.getStatusMessages().get(2));
    }

    public void testUpdateToRevision() throws ScmException, IOException, ParseException
    {
        client.setBranch("master");
        client.checkout(context, null, handler);
        assertEquals("", IOUtils.fileToString(new File(workingDir, "a.txt")));
        
        Revision rev = client.update(context, new Revision("b69a48a6b0f567d0be110c1fbca2c48fc3e1b112"), handler);
        assertEquals("b69a48a6b0f567d0be110c1fbca2c48fc3e1b112", rev.getRevisionString());

        assertEquals("content", IOUtils.fileToString(new File(workingDir, "a.txt")));
    }

    public void testChanges() throws ScmException
    {
        client.setBranch("master");
        List<Changelist> changes = client.getChanges(scmContext, new Revision("HEAD~2"), null);
        assertEquals(2, changes.size());
    }

    public void testHeadChanges() throws ScmException
    {
        client.setBranch("master");
        List<Changelist> changes = client.getChanges(scmContext, new Revision("HEAD~1"), new Revision("HEAD"));
        assertEquals(1, changes.size());
        Changelist changelist = changes.get(0);
        assertEquals("removed content from a.txt", changelist.getComment());
        assertEquals("e34da05e88de03a4aa5b10b338382f09bbe65d4b", changelist.getRevision().getRevisionString());
        assertEquals("Daniel Ostermeier <daniel@zutubi.com>", changelist.getAuthor());
        assertEquals(1, changelist.getChanges().size());
        FileChange change = changelist.getChanges().get(0);
        assertEquals(FileChange.Action.EDIT, change.getAction());
        assertEquals("a.txt", change.getPath());
    }

    private Date parse(String str) throws ParseException
    {
        return new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z").parse(str);
    }

    private String readToString(InputStream content) throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.joinStreams(content, out);
        return out.toString();
    }
}
