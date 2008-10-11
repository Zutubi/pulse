package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.BuildProperties;
import com.zutubi.pulse.core.ExecutionContext;
import com.zutubi.pulse.core.scm.RecordingScmFeedbackHandler;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.pulse.core.util.ZipUtils;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 */
public class PerforceClientTest extends PulseTestCase
{
    private static final String TEST_CLIENT = "test-client";

    private PerforceCore core;
    private PerforceClient client;
    private File tmpDir;
    private File workDir;
    private Process p4dProcess;
    private boolean generateMode = false;

    protected void setUp() throws Exception
    {
        super.setUp();
        core = new PerforceCore();

        tmpDir = FileSystemUtils.createTempDir(getClass().getName(), "");
        File repoDir = new File(tmpDir, "repo");

        File repoZip = getTestDataFile("bundles/com.zutubi.pulse.core.scm.p4", "repo", "zip");
        ZipUtils.extractZip(repoZip, repoDir);

        // Restore from checkpoint
        p4dProcess = Runtime.getRuntime().exec(new String[] { "p4d", "-r", repoDir.getAbsolutePath(), "-jr", "checkpoint.1"});
        p4dProcess.waitFor();

        p4dProcess = Runtime.getRuntime().exec(new String[] { "p4d", "-r", repoDir.getAbsolutePath(), "-p", "6666"});

        workDir = new File(tmpDir, "work");
        workDir.mkdirs();

        waitForServer(6666);
    }

    protected void tearDown() throws Exception
    {
        core = null;
        client = null;

        p4dProcess.destroy();
        Thread.sleep(400);
        removeDirectory(tmpDir);
        super.tearDown();
    }

    public void testGetLocation() throws ScmException
    {
        getServer(TEST_CLIENT);
        assertEquals(client.getLocation(), "test-client@:6666");
    }

    public void testGetLatestRevision() throws ScmException
    {
        getServer(TEST_CLIENT);
        assertEquals("8", client.getLatestRevision(null).getRevisionString());
    }

    public void testCheckoutHead() throws Exception
    {
        getServer("depot-client");
        List<String> statuses = checkoutChanges(null, workDir, null, 8);

        assertEquals(10, statuses.size());
        for (int i = 0; i < 10; i++)
        {
            String status = statuses.get(i);

            // Foolish of me to create file10 which ruins lexical ordering :|
            int number;
            if (i == 0)
            {
                number = 1;
            }
            else if (i == 1)
            {
                number = 10;
            }
            else
            {
                number = i;
            }

            String re = String.format("//depot/file%d#[1-9][0-9]* - added as.*", number);
            assertTrue("Status '" + status + "' does not match expected format '" + re + "'", status.matches(re));
        }

        checkDirectory("checkoutHead");
    }

    public void testCheckoutRevision() throws Exception
    {
        getServer("depot-client");

        Revision revision = client.checkout(createExecutionContext(workDir, null), createRevision(1), null);
        assertEquals("1", revision.getRevisionString());
        checkDirectory("checkoutRevision");
    }

    public void testCheckoutFile() throws ScmException, IOException
    {
        getServer("depot-client");
        String content = IOUtils.inputStreamToString(client.retrieve(null, FileSystemUtils.composeFilename("depot", "file2"), null));
        assertEquals("content of file2: edited at the same time as file2 in depot2.\n", content);
    }

    public void testCheckoutFileRevision() throws ScmException, IOException
    {
        getServer("depot-client");
        String content = IOUtils.inputStreamToString(client.retrieve(null, FileSystemUtils.composeFilename("depot", "file2"), createRevision(2)));
        assertEquals("content of file2\n", content);
    }

    public void testGetChanges() throws Exception
    {
        // [{ uid: :6666, rev: 7, changes: [//depot2/test-branch/file9#2 - INTEGRATE] },
        //  { uid: :6666, rev: 6, changes: [//depot2/file9#2 - EDIT] },
        //  { uid: :6666, rev: 5, changes: [//depot2/test-branch/file1#1 - BRANCH, //depot2/test-branch/file2#1 - BRANCH, //depot2/test-branch/file3#1 - BRANCH, //depot2/test-branch/file4#1 - BRANCH, //depot2/test-branch/file5#1 - BRANCH, //depot2/test-branch/file6#1 - BRANCH, //depot2/test-branch/file7#1 - BRANCH, //depot2/test-branch/file8#1 - BRANCH, //depot2/test-branch/file9#1 - BRANCH] },
        //  { uid: :6666, rev: 4, changes: [//depot/file2#2 - EDIT, //depot2/file2#2 - EDIT] },
        //  { uid: :6666, rev: 3, changes: [//depot2/file1#2 - EDIT, //depot2/file10#2 - DELETE] },
        //  { uid: :6666, rev: 2, changes: [//depot2/file1#1 - ADD, //depot2/file10#1 - ADD, //depot2/file2#1 - ADD, //depot2/file3#1 - ADD, //depot2/file4#1 - ADD, //depot2/file5#1 - ADD, //depot2/file6#1 - ADD, //depot2/file7#1 - ADD, //depot2/file8#1 - ADD, //depot2/file9#1 - ADD] }]
        getServer(TEST_CLIENT);
        List<Changelist> changes = client.getChanges(null, createRevision(1), createRevision(7));
        assertEquals(6, changes.size());
        Changelist list = changes.get(1);
        assertEquals("Delete and edit files in depot2.", list.getComment());
        assertEquals("test-user", list.getAuthor());
        assertEquals("3", list.getRevision().getRevisionString());
        List<FileChange> changedFiles = list.getChanges();
        assertEquals(2, changedFiles.size());
        FileChange file1 = changedFiles.get(0);
        assertEquals("//depot2/file1", file1.getPath());
        assertEquals(FileChange.Action.EDIT, file1.getAction());
        FileChange file10 = changedFiles.get(1);
        assertEquals("//depot2/file10", file10.getPath());
        assertEquals(FileChange.Action.DELETE, file10.getAction());
    }

    public void testGetChangesRestrictedToView() throws Exception
    {
        getServer("depot-client");
        List<Changelist> changes = client.getChanges(null, createRevision(1), createRevision(7));
        assertEquals(1, changes.size());
        assertEquals("4", (changes.get(0).getRevision()).getRevisionString());
    }

    public void testListNonExistent() throws ScmException
    {
        getServer(TEST_CLIENT);
        try
        {
            client.browse(null, "depot4", null);
            fail();
        }
        catch (ScmException e)
        {
            assertTrue(e.getMessage().contains("does not exist"));
        }
    }

    public void testListRoot() throws ScmException
    {
        getServer(TEST_CLIENT);
        List<ScmFile> files = client.browse(null, "", null);
        assertEquals(2, files.size());
        ScmFile f = files.get(0);
        assertEquals("depot", f.getName());
        assertTrue(f.isDirectory());

        f = files.get(1);
        assertEquals("depot2", f.getName());
        assertEquals("depot2", f.getPath());
        assertTrue(f.isDirectory());
    }

    public void testListPath() throws ScmException
    {
        getServer(TEST_CLIENT);
        List<ScmFile> files = client.browse(null, "depot2", null);
        assertEquals(10, files.size());

        ScmFile f;
        for (int i = 0; i < 9; i++)
        {
            f = files.get(i);
            assertTrue(f.isFile());
            assertEquals("file" + (i + 1), f.getName());
            assertEquals("depot2/file" + (i + 1), f.getPath());
        }

        f = files.get(9);
        assertEquals("test-branch", f.getName());
        assertEquals("depot2/test-branch", f.getPath());
        assertTrue(f.isDirectory());
    }

    public void testListComplexClient() throws ScmException
    {
        getServer("complex-client");
        List<ScmFile> files = client.browse(null, "", null);
        assertEquals(1, files.size());
        ScmFile scmFile = files.get(0);
        assertEquals("src", scmFile.getName());
        assertTrue(scmFile.isDirectory());
    }

    public void testListComplexSrc() throws ScmException
    {
        getServer("complex-client");
        List<ScmFile> files = client.browse(null, "src", null);
        assertEquals(2, files.size());

        ScmFile scmFile = files.get(0);
        assertEquals("host", scmFile.getName());
        assertTrue(scmFile.isDirectory());

        scmFile = files.get(1);
        assertEquals("libraries", scmFile.getName());
        assertTrue(scmFile.isDirectory());
    }

    public void testListComplexSnuth() throws ScmException
    {
        getServer("complex-client");
        List<ScmFile> files = client.browse(null, "src/libraries/snuth", null);
        assertEquals(2, files.size());

        ScmFile scmFile = files.get(0);
        assertEquals("Makefile", scmFile.getName());
        assertFalse(scmFile.isDirectory());

        scmFile = files.get(1);
        assertEquals("source.c", scmFile.getName());
        assertFalse(scmFile.isDirectory());
    }

    public void testTag() throws ScmException
    {
        getServer(TEST_CLIENT);
        assertFalse(client.labelExists(TEST_CLIENT, "test-tag"));
        client.tag(null, createRevision(5), "test-tag", false);
        assertTrue(client.labelExists(TEST_CLIENT, "test-tag"));
        PerforceCore.P4Result result = core.runP4(null, "p4", "-c", TEST_CLIENT, "sync", "-f", "-n", "@test-tag");
        assertTrue(result.stdout.toString().contains("//depot2/file9#1"));
    }

    public void testMoveTag() throws ScmException
    {
        testTag();
        client.tag(null, createRevision(7), "test-tag", true);
        assertTrue(client.labelExists(TEST_CLIENT, "test-tag"));
        PerforceCore.P4Result result = core.runP4(null, "p4", "-c", TEST_CLIENT, "sync", "-f", "-n", "@test-tag");
        assertTrue(result.stdout.toString().contains("//depot2/file9#2"));
    }

    public void testUnmovableTag() throws ScmException
    {
        getServer(TEST_CLIENT);
        client.tag(null, createRevision(5), "test-tag", false);
        assertTrue(client.labelExists(TEST_CLIENT, "test-tag"));
        try
        {
            client.tag(null, createRevision(7), "test-tag", false);
            fail();
        }
        catch(ScmException e)
        {
            assertEquals(e.getMessage(), "Cannot create label 'test-tag': label already exists");
        }
    }

    public void testTagSameRevision() throws ScmException
    {
        getServer(TEST_CLIENT);
        client.tag(null, createRevision(5), "test-tag", false);
        assertTrue(client.labelExists(TEST_CLIENT, "test-tag"));
        client.tag(null, createRevision(5), "test-tag", true);
    }

    public void testGetRevisionsSince() throws ScmException
    {
        getServer(TEST_CLIENT);
        List<Revision> revisions = client.getRevisions(null, createRevision(5), null);
        assertEquals(2, revisions.size());
        assertEquals("6", revisions.get(0).getRevisionString());
        assertEquals("7", revisions.get(1).getRevisionString());
    }

    public void testGetRevisionsSinceLatest() throws ScmException
    {
        getServer(TEST_CLIENT);
        List<Revision> revisions = client.getRevisions(null, createRevision(7), null);
        assertEquals(0, revisions.size());
    }

    public void testGetRevisionsSinceFiltered() throws ScmException
    {
        getServer(TEST_CLIENT);
        client.setExcludedPaths(Arrays.asList("//depot2/*"));
        List<Revision> revisions = client.getRevisions(null, createRevision(5), null);
        assertEquals(1, revisions.size());
        assertEquals("7", revisions.get(0).getRevisionString());
    }

    public void testCheckoutThenUpdate() throws ScmException, IOException
    {
        getServer("depot-client");

        Revision got = client.checkout(createExecutionContext(workDir, "my-id"), createRevision(1), null);
        assertEquals("1", got.getRevisionString());
        checkDirectory("checkoutRevision");

        List<String> statuses = updateChanges("my-id", workDir, createRevision(8));
        checkDirectory("checkoutHead");
        assertEquals(1, statuses.size());
        assertTrue(statuses.get(0).startsWith("//depot/file2#2 - updating"));
    }

    public void testUpdateSameRevision() throws ScmException, IOException
    {
        getServer("depot-client");

        client.checkout(createExecutionContext(workDir, "my-id"), null, null);

        List<String> statuses = updateChanges("my-id", workDir, null);
        checkDirectory("checkoutHead");
        assertEquals(0, statuses.size());
    }

    public void testMultiUpdates() throws ScmException, IOException
    {
        getServer(TEST_CLIENT);

        client.checkout(createExecutionContext(workDir, null), createRevision(1), null);

        for(int i = 2; i <= 8; i++)
        {
            Revision updateRevision = createRevision(i);
            updateChanges("my-id", workDir, updateRevision);
        }
    }

    public void testGetFileRevision() throws ScmException
    {
        getServer(TEST_CLIENT);
        assertEquals("2", client.getFileRevision("//depot2/file1", createRevision(5)));
    }

    public void testGetFileRevisionUnknownPath() throws ScmException
    {
        getServer(TEST_CLIENT);
        assertNull(client.getFileRevision("//depot2/this/path/is/wrong", createRevision(5)));
    }

    public void testGetFileRevisionBeforeFileAdded() throws ScmException
    {
        getServer(TEST_CLIENT);
        assertNull(client.getFileRevision("//depot2/file1", createRevision(1)));
    }

    public void testGetFileRevisionAfterFileDeleted() throws ScmException
    {
        getServer(TEST_CLIENT);
        assertNull(client.getFileRevision("//depot2/file10", createRevision(4)));
    }

    public void testGetRevisionLatest() throws ScmException
    {
        getServer(TEST_CLIENT);
        Revision latest = client.getLatestRevision(null);
        Revision rev = client.parseRevision(latest.getRevisionString());
        assertEquals(latest.getRevisionString(), rev.getRevisionString());
    }

    public void testGetRevisionPostLatest() throws ScmException
    {
        getServer(TEST_CLIENT);
        Revision latest = client.getLatestRevision(null);
        try
        {
            client.parseRevision(Long.toString(Long.valueOf(latest.toString()) + 1));
            fail();
        }
        catch (ScmException e)
        {
            assertTrue(e.getMessage().matches(".*Change [0-9]+ unknown.*"));
        }
    }

    public void testGetRevisionInvalid() throws ScmException
    {
        getServer(TEST_CLIENT);
        try
        {
            client.parseRevision("bullet");
            fail();
        }
        catch (ScmException e)
        {
            assertEquals("Invalid revision 'bullet': must be a valid Perforce changelist number", e.getMessage());
        }
    }

    public void testLockedTemplate() throws ScmException, IOException
    {
        String spec = SystemUtils.runCommand("p4", "-p", "6666", "client", "-o", TEST_CLIENT);
        spec = spec.replaceAll("(\nOptions:.*)unlocked", "$1locked");
        SystemUtils.runCommandWithInput(spec, "p4", "-p", "6666", "client", "-i");

        PerforceCore core = getClient();
        core.createClient(TEST_CLIENT, "unlocked-client", tmpDir);
        spec = SystemUtils.runCommand("p4", "-p", "6666", "client", "-o", TEST_CLIENT);
        assertTrue(spec.contains(" locked"));
        spec = SystemUtils.runCommand("p4", "-p", "6666", "client", "-o", "unlocked-client");
        assertFalse(spec.contains(" locked"));
    }

    public void testDeletedChangelist() throws Exception
    {
        // CIB-1010
        getServer(TEST_CLIENT);

        Revision latest = client.getLatestRevision(null);

        PerforceCore core = getClient();
        core.setEnv(PerforceConstants.ENV_CLIENT, TEST_CLIENT);
        core.runP4("Change: new\n" +
                     "Client: test-client\n" +
                     "User: test-user\n" +
                     "Status: new\n" +
                     "Description:\n" +
                     "    Dead changelist",
                     "p4", "change", "-i");
        core.runP4(null, "p4", "change", "-d", Long.toString(Long.valueOf(latest.getRevisionString()) + 1));

        core.createClient(TEST_CLIENT, "edit-client", workDir);
        core.setEnv(PerforceConstants.ENV_CLIENT, "edit-client");
        core.runP4(null, "p4", "sync");
        core.runP4(null, "p4", "edit", "//depot/file1");
        core.submit("test edit");
        core.setEnv(PerforceConstants.ENV_CLIENT, TEST_CLIENT);
        core.runP4(null, "p4", "client", "-d", "edit-client");

        client.setExcludedPaths(Arrays.asList("//depot/file2"));
        assertTrue(client.getRevisions(null, latest, null).size() > 0);
    }

    private PerforceCore getClient()
    {
        PerforceCore core = new PerforceCore();
        core.setEnv(PerforceConstants.ENV_PORT, ":6666");
        core.setEnv(PerforceConstants.ENV_USER, "test-user");
        return core;
    }

    private void getServer(String client) throws ScmException
    {
        this.client = new PerforceClient(":6666", "test-user", "", client);
        this.core.setEnv(PerforceConstants.ENV_PORT, ":6666");
        this.core.setEnv(PerforceConstants.ENV_USER, "test-user");
    }

    private void checkDirectory(String name) throws IOException
    {
        File expectedRoot = getDataRoot();
        File expectedDir = new File(expectedRoot, name);

        if (generateMode)
        {
            workDir.renameTo(expectedDir);
        }
        else
        {
            assertDirectoriesEqual(expectedDir, workDir);
        }
    }

    private File getDataRoot()
    {
        return new File(getPulseRoot(), FileSystemUtils.composeFilename("bundles", "com.zutubi.pulse.core.scm.p4", "src", "test", "com", "zutubi", "pulse", "core", "scm", "p4", "data"));
    }

    private List<String> checkoutChanges(String id, File dir, Revision revision, long expectedRevision) throws ScmException
    {
        ExecutionContext context = createExecutionContext(dir, id);
        RecordingScmFeedbackHandler handler = new RecordingScmFeedbackHandler();
        Revision rev = client.checkout(context, revision, handler);
        assertEquals(expectedRevision, (long)Long.valueOf(rev.getRevisionString()));
        return handler.getStatusMessages();
    }

    private List<String> updateChanges(String id, File dir, Revision revision) throws ScmException
    {
        ExecutionContext context = createExecutionContext(dir, id);
        RecordingScmFeedbackHandler handler = new RecordingScmFeedbackHandler();
        client.update(context, revision, handler);
        return handler.getStatusMessages();
    }

    private ExecutionContext createExecutionContext(File dir, String id)
    {
        ExecutionContext context = new ExecutionContext();
        context.getScope().setLabel(BuildProperties.SCOPE_RECIPE);
        context.setWorkingDir(dir);
        if (id != null)
        {
            context.addString("scm.bootstrap.id", id);
        }
        return context;
    }

    private Revision createRevision(long rev)
    {
        return new Revision(Long.toString(rev));
    }
}
