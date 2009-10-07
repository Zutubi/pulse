package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.scm.RecordingScmFeedbackHandler;
import com.zutubi.pulse.core.scm.ScmContextImpl;
import com.zutubi.pulse.core.scm.api.*;
import static com.zutubi.pulse.core.scm.p4.PerforceConstants.FLAG_CLIENT;
import com.zutubi.pulse.core.scm.p4.config.PerforceConfiguration;
import com.zutubi.pulse.core.test.IOAssertions;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.io.IOUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class PerforceClientTest extends PerforceTestBase
{
    private static final String DIR_EXPECTED = "expected";

    private static final String FILE_CHECKPOINT = "checkpoint.2";

    private static final String DEPOT_WORKSPACE = "depot-client";
    private static final String TEST_WORKSPACE = "test-client";

    private static final String LABEL_TWO = "two";
    private static final String LABEL_LATEST = "latest";
    
    private PerforceCore core;
    private PerforceClient client;
    private File workDir;

    protected String getCheckpointFilename()
    {
        return FILE_CHECKPOINT;
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        core = new PerforceCore();

        workDir = new File(getTempDir(), "work");
        if(!workDir.mkdirs())
        {
            throw new RuntimeException("Unable to make work directory '" + workDir.getAbsolutePath() + "'");
        }
    }

    public void testGetLocation() throws ScmException
    {
        getServer(TEST_WORKSPACE);
        assertEquals(client.getLocation(), "test-client@" + getP4Port());
    }

    public void testNonExistantTemplateWorkspaceFails() throws ScmException
    {
        final String WORKSPACE_NAME = "i-do-not-exist";
        getServer(WORKSPACE_NAME);
        try
        {
            client.getLatestRevision(null).getRevisionString();
            fail("Should not be able to use non-existant template workspace.");
        }
        catch (ScmException e)
        {
            // The message is going to the user, and we label it as a client
            // in the interface.
            assertThat(e.getMessage(), containsString("Template client '" + WORKSPACE_NAME + "' does not exist."));
        }
    }

    public void testGetLatestRevision() throws ScmException
    {
        getServer(TEST_WORKSPACE);
        assertEquals("7", client.getLatestRevision(null).getRevisionString());
    }

    public void testGetLatestRevisionRestrictedToView() throws ScmException
    {
        getServer(DEPOT_WORKSPACE);
        assertEquals("4", client.getLatestRevision(null).getRevisionString());
    }

    public void testGetRevisionsLatest() throws ScmException
    {
        getServer(DEPOT_WORKSPACE);
        Revision latest = client.getLatestRevision(null);
        assertEquals(0, client.getRevisions(null, latest, null).size());
    }

    public void testGetRevisions() throws ScmException
    {
        getServer(DEPOT_WORKSPACE);
        Revision latest = client.getLatestRevision(null);
        List<Revision> revisions = client.getRevisions(null, latest.calculatePreviousNumericalRevision(), null);
        assertEquals(1, revisions.size());
        assertEquals(latest, revisions.get(0));
    }

    public void testGetRevisionsBetweenLabels() throws ScmException
    {
        getServer(TEST_WORKSPACE);
        List<Revision> revisions = client.getRevisions(null, new Revision(LABEL_TWO), new Revision(LABEL_LATEST));
        // The latest thing in our view is revision 7, so we expect revisions 3-7.
        assertEquals(5, revisions.size());
        assertEquals("3", revisions.get(0).getRevisionString());
        assertEquals("7", revisions.get(4).getRevisionString());
    }

    public void testCheckoutHead() throws Exception
    {
        getServer(DEPOT_WORKSPACE);
        List<String> statuses = checkoutChanges(workDir, null, 4);

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
        getServer(DEPOT_WORKSPACE);

        Revision revision = client.checkout(createExecutionContext(workDir, false), createRevision(1), null);
        assertEquals("1", revision.getRevisionString());
        checkDirectory("checkoutRevision");
    }

    public void testSyncWorkspacePersists() throws ScmException, IOException
    {
        getServer(DEPOT_WORKSPACE);

        ExecutionContext context = createExecutionContext(workDir, false);
        client.checkout(context, createRevision(1), null);
        assertTrue(core.workspaceExists(PerforceWorkspaceManager.getSyncWorkspaceName(new PerforceConfiguration(), context)));
    }

    public void testSyncWorkspaceCleanedOnDestroy() throws ScmException, IOException
    {
        getServer(DEPOT_WORKSPACE);

        ExecutionContext context = createExecutionContext(workDir, false);
        client.checkout(context, createRevision(1), null);
        String workspaceName = PerforceWorkspaceManager.getSyncWorkspaceName(new PerforceConfiguration(), context);
        assertTrue(core.workspaceExists(workspaceName));

        client.destroy(createScmContext(), new RecordingScmFeedbackHandler());
        assertFalse(core.workspaceExists(workspaceName));
    }

    public void testCheckoutFile() throws ScmException, IOException
    {
        getServer(DEPOT_WORKSPACE);
        String content = IOUtils.inputStreamToString(client.retrieve(null, FileSystemUtils.composeFilename("depot", "file2"), null));
        assertEquals("content of file2: edited at the same time as file2 in depot2.\n", content);
    }

    public void testCheckoutFileRevision() throws ScmException, IOException
    {
        getServer(DEPOT_WORKSPACE);
        String content = IOUtils.inputStreamToString(client.retrieve(null, FileSystemUtils.composeFilename("depot", "file2"), createRevision(2)));
        assertEquals("content of file2\n", content);
    }

    public void testProjectWorkspacePersists() throws ScmException
    {
        getServer(DEPOT_WORKSPACE);

        client.retrieve(createScmContext(), FileSystemUtils.composeFilename("depot", "file2"), null);
        assertTrue(core.workspaceExists("pulse-" + TEST_PROJECT_HANDLE));
    }

    public void testProjectWorkspaceCleanedOnDestroy() throws ScmException
    {
        getServer(DEPOT_WORKSPACE);

        ScmContextImpl context = createScmContext();
        client.retrieve(context, FileSystemUtils.composeFilename("depot", "file2"), null);
        String workspace = PerforceWorkspaceManager.getWorkspacePrefix(TEST_PROJECT_HANDLE);
        assertTrue(core.workspaceExists(workspace));

        client.destroy(context, new RecordingScmFeedbackHandler());
        assertFalse(core.workspaceExists(workspace));
    }

    public void testGetChanges() throws Exception
    {
        // [{ uid: :6666, rev: 7, changes: [//depot2/test-branch/file9#2 - INTEGRATE] },
        //  { uid: :6666, rev: 6, changes: [//depot2/file9#2 - EDIT] },
        //  { uid: :6666, rev: 5, changes: [//depot2/test-branch/file1#1 - BRANCH, //depot2/test-branch/file2#1 - BRANCH, //depot2/test-branch/file3#1 - BRANCH, //depot2/test-branch/file4#1 - BRANCH, //depot2/test-branch/file5#1 - BRANCH, //depot2/test-branch/file6#1 - BRANCH, //depot2/test-branch/file7#1 - BRANCH, //depot2/test-branch/file8#1 - BRANCH, //depot2/test-branch/file9#1 - BRANCH] },
        //  { uid: :6666, rev: 4, changes: [//depot/file2#2 - EDIT, //depot2/file2#2 - EDIT] },
        //  { uid: :6666, rev: 3, changes: [//depot2/file1#2 - EDIT, //depot2/file10#2 - DELETE] },
        //  { uid: :6666, rev: 2, changes: [//depot2/file1#1 - ADD, //depot2/file10#1 - ADD, //depot2/file2#1 - ADD, //depot2/file3#1 - ADD, //depot2/file4#1 - ADD, //depot2/file5#1 - ADD, //depot2/file6#1 - ADD, //depot2/file7#1 - ADD, //depot2/file8#1 - ADD, //depot2/file9#1 - ADD] }]
        getServer(TEST_WORKSPACE);
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

    public void testGetChangesReverseRange() throws Exception
    {
        getServer(TEST_WORKSPACE);
        List<Changelist> changes = client.getChanges(null, createRevision(4), createRevision(2));
        assertTrue(changes.isEmpty());
    }

    public void testGetChangesRestrictedToView() throws Exception
    {
        getServer(DEPOT_WORKSPACE);
        List<Changelist> changes = client.getChanges(null, createRevision(1), createRevision(7));
        assertEquals(1, changes.size());
        assertEquals("4", (changes.get(0).getRevision()).getRevisionString());
    }

    public void testListNonExistent() throws ScmException
    {
        getServer(TEST_WORKSPACE);
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
        getServer(TEST_WORKSPACE);
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
        getServer(TEST_WORKSPACE);
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
        getServer(TEST_WORKSPACE);
        assertFalse(client.labelExists(TEST_WORKSPACE, "test-tag"));
        client.tag(null, null, createRevision(5), "test-tag", false);
        assertTrue(client.labelExists(TEST_WORKSPACE, "test-tag"));
        PerforceCore.P4Result result = core.runP4(null, "p4", "-c", TEST_WORKSPACE, "sync", "-f", "-n", "@test-tag");
        assertTrue(result.stdout.toString().contains("//depot2/file9#1"));
    }

    public void testMoveTag() throws ScmException
    {
        testTag();
        client.tag(null, null, createRevision(7), "test-tag", true);
        assertTrue(client.labelExists(TEST_WORKSPACE, "test-tag"));
        PerforceCore.P4Result result = core.runP4(null, "p4", "-c", TEST_WORKSPACE, "sync", "-f", "-n", "@test-tag");
        assertTrue(result.stdout.toString().contains("//depot2/file9#2"));
    }

    public void testUnmovableTag() throws ScmException
    {
        getServer(TEST_WORKSPACE);
        client.tag(null, null, createRevision(5), "test-tag", false);
        assertTrue(client.labelExists(TEST_WORKSPACE, "test-tag"));
        try
        {
            client.tag(null, null, createRevision(7), "test-tag", false);
            fail();
        }
        catch(ScmException e)
        {
            assertEquals(e.getMessage(), "Cannot create label 'test-tag': label already exists");
        }
    }

    public void testTagSameRevision() throws ScmException
    {
        getServer(TEST_WORKSPACE);
        client.tag(null, null, createRevision(5), "test-tag", false);
        assertTrue(client.labelExists(TEST_WORKSPACE, "test-tag"));
        client.tag(null, null, createRevision(5), "test-tag", true);
    }

    public void testGetRevisionsSince() throws ScmException
    {
        getServer(TEST_WORKSPACE);
        List<Revision> revisions = client.getRevisions(null, createRevision(5), null);
        assertEquals(2, revisions.size());
        assertEquals("6", revisions.get(0).getRevisionString());
        assertEquals("7", revisions.get(1).getRevisionString());
    }

    public void testGetRevisionsSinceLatest() throws ScmException
    {
        getServer(TEST_WORKSPACE);
        List<Revision> revisions = client.getRevisions(null, createRevision(7), null);
        assertEquals(0, revisions.size());
    }

    public void testGetRevisionsSinceFiltered() throws ScmException
    {
        getServer(TEST_WORKSPACE, "//depot2/*");
        List<Revision> revisions = client.getRevisions(null, createRevision(5), null);
        assertEquals(1, revisions.size());
        assertEquals("7", revisions.get(0).getRevisionString());
    }

    public void testCheckoutThenUpdate() throws ScmException, IOException
    {
        getServer(DEPOT_WORKSPACE);

        Revision got = client.checkout(createExecutionContext(workDir, true), createRevision(1), null);
        assertEquals("1", got.getRevisionString());
        checkDirectory("checkoutRevision");

        List<String> statuses = updateChanges(workDir, createRevision(8));
        checkDirectory("checkoutHead");
        assertEquals(1, statuses.size());
        assertTrue(statuses.get(0).startsWith("//depot/file2#2 - updating"));
    }

    public void testUpdateSameRevision() throws ScmException, IOException
    {
        getServer(DEPOT_WORKSPACE);

        client.checkout(createExecutionContext(workDir, true), null, null);

        List<String> statuses = updateChanges(workDir, null);
        checkDirectory("checkoutHead");
        assertEquals(0, statuses.size());
    }

    public void testMultiUpdates() throws ScmException, IOException
    {
        getServer(TEST_WORKSPACE);

        client.checkout(createExecutionContext(workDir, true), createRevision(1), null);

        for(int i = 2; i <= 8; i++)
        {
            Revision updateRevision = createRevision(i);
            updateChanges(workDir, updateRevision);
        }
    }

    public void testParseRevisionLatest() throws ScmException
    {
        getServer(TEST_WORKSPACE);
        Revision latest = client.getLatestRevision(null);
        Revision rev = client.parseRevision(null, latest.getRevisionString());
        assertEquals(latest.getRevisionString(), rev.getRevisionString());
    }

    public void testParseRevisionLabel() throws ScmException
    {
        getServer(TEST_WORKSPACE);
        Revision rev = client.parseRevision(null, LABEL_TWO);
        assertEquals(LABEL_TWO, rev.getRevisionString());
    }

    public void testParseRevisionNegative() throws ScmException
    {
        failedParseRevisionHelper("-1", "Invalid changelist/client/label/date");
    }

    public void testParseRevisionZero() throws ScmException
    {
        failedParseRevisionHelper("0", "No changelists found for revision '0'");
    }

    public void testParseRevisionPostLatest() throws ScmException
    {
        // Perforce happily accepts such a revision, but we will map it to
        // the equivalent that makes sense.
        getServer(TEST_WORKSPACE);
        Revision rev = client.parseRevision(null, "10000");
        assertEquals(client.getLatestRevision(null).toString(), rev.getRevisionString());
    }

    public void testParseRevisionInvalid() throws ScmException
    {
        failedParseRevisionHelper("bullet", "Invalid changelist/client/label/date");
    }

    private void failedParseRevisionHelper(String revision, String expectedMessage) throws ScmException
    {
        getServer(TEST_WORKSPACE);
        try
        {
            client.parseRevision(null, revision);
            fail();
        }
        catch (ScmException e)
        {
            assertTrue(e.getMessage().contains(expectedMessage));
        }
    }

    public void testLockedTemplate() throws ScmException, IOException
    {
        String spec = SystemUtils.runCommand("p4", "-p", "6666", "client", "-o", TEST_WORKSPACE);
        spec = spec.replaceAll("(\nOptions:.*)unlocked", "$1locked");
        SystemUtils.runCommandWithInput(spec, "p4", "-p", "6666", "client", "-i");

        PerforceCore core = getClient();
        core.createOrUpdateWorkspace(TEST_WORKSPACE, "unlocked-client", "description", getTempDir().getAbsolutePath());
        spec = SystemUtils.runCommand("p4", "-p", "6666", "client", "-o", TEST_WORKSPACE);
        assertTrue(spec.contains(" locked"));
        spec = SystemUtils.runCommand("p4", "-p", "6666", "client", "-o", "unlocked-client");
        assertFalse(spec.contains(" locked"));
    }

    public void testDeletedChangelist() throws Exception
    {
        // CIB-1010
        getServer(TEST_WORKSPACE, "//depot/file2");

        PerforceCore core = getClient();
        core.runP4("Change: new\n" +
                     "Client: test-client\n" +
                     "User: test-user\n" +
                     "Status: new\n" +
                     "Description:\n" +
                     "    Dead changelist",
                     "p4", FLAG_CLIENT, TEST_WORKSPACE, "change", "-i");
        core.runP4(null, "p4", FLAG_CLIENT, TEST_WORKSPACE, "change", "-d", "9");

        core.createOrUpdateWorkspace(TEST_WORKSPACE, "edit-client", "description", workDir.getAbsolutePath());
        core.setEnv(PerforceConstants.ENV_CLIENT, "edit-client");
        core.runP4(null, "p4", "sync");
        core.runP4(null, "p4", "edit", "//depot/file1");
        core.submit("test edit");
        core.runP4(null, "p4", "client", "-d", "edit-client");

        assertTrue(client.getRevisions(null, new Revision(7), null).size() > 0);
    }

    public void testGetEmail() throws ScmException
    {
        getServer(TEST_WORKSPACE);
        assertEquals("jsankey@bob", client.getEmailAddress(null, "jsankey"));
    }

    private PerforceCore getClient()
    {
        PerforceCore core = new PerforceCore();
        core.setEnv(PerforceConstants.ENV_PORT, getP4Port());
        core.setEnv(PerforceConstants.ENV_USER, "test-user");
        return core;
    }

    private void getServer(String workspace, String... excludedPaths) throws ScmException
    {
        PerforceConfiguration configuration = new PerforceConfiguration(getP4Port(), "test-user", "", workspace);
        configuration.setFilterPaths(Arrays.asList(excludedPaths));
        this.client = new PerforceClient(configuration, new PerforceWorkspaceManager());
        this.core.setEnv(PerforceConstants.ENV_PORT, getP4Port());
        this.core.setEnv(PerforceConstants.ENV_USER, "test-user");
    }

    private void checkDirectory(String name) throws IOException
    {
        File expectedDir = new File(getTempDir(), DIR_EXPECTED);
        removeDirectory(expectedDir);
        assertTrue(expectedDir.mkdirs());
        unzipInput(name, expectedDir);

        IOAssertions.assertDirectoriesEqual(expectedDir, workDir);
    }

    private List<String> checkoutChanges(File dir, Revision revision, long expectedRevision) throws ScmException
    {
        ExecutionContext context = createExecutionContext(dir, false);
        RecordingScmFeedbackHandler handler = new RecordingScmFeedbackHandler();
        Revision rev = client.checkout(context, revision, handler);
        assertEquals(expectedRevision, Long.parseLong(rev.getRevisionString()));
        return handler.getStatusMessages();
    }

    private List<String> updateChanges(File dir, Revision revision) throws ScmException
    {
        ExecutionContext context = createExecutionContext(dir, true);
        RecordingScmFeedbackHandler handler = new RecordingScmFeedbackHandler();
        client.update(context, revision, handler);
        return handler.getStatusMessages();
    }

    private ScmContextImpl createScmContext()
    {
        ScmContextImpl scmContext = new ScmContextImpl();
        scmContext.setProjectHandle(TEST_PROJECT_HANDLE);
        scmContext.setProjectName(TEST_PROJECT);
        scmContext.setPersistentWorkingDir(workDir);
        return scmContext;
    }

    private Revision createRevision(long rev)
    {
        return new Revision(Long.toString(rev));
    }
}
