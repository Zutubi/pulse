package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.scm.RecordingScmFeedbackHandler;
import com.zutubi.pulse.core.scm.ScmContextImpl;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.p4.config.PerforceConfiguration;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.IOAssertions;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.zutubi.pulse.core.scm.p4.PerforceConstants.FLAG_CLIENT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class PerforceClientTest extends PerforceTestBase
{
    private static final String DIR_EXPECTED = "expected";

    private static final String DEPOT_WORKSPACE = "depot-client";
    private static final String TEST_WORKSPACE = "test-client";

    private static final String LABEL_TWO = "two";
    private static final String LABEL_LATEST = "latest";
    
    private PerforceCore core;
    private PerforceClient client;
    private File workDir;

    protected void setUp() throws Exception
    {
        super.setUp();
        
        workDir = new File(tmpDir, "work");
        if(!workDir.mkdirs())
        {
            throw new RuntimeException("Unable to make work directory '" + workDir.getAbsolutePath() + "'");
        }

        deployPerforceServer("repo", P4D_PORT, 2, false);
        core = new PerforceCore();
    }

    public void testGetLocation() throws ScmException
    {
        setupClient(TEST_WORKSPACE);
        assertEquals(client.getLocation(), "test-client@" + getP4Port());
    }

    public void testNonExistantTemplateWorkspaceFails() throws ScmException
    {
        final String WORKSPACE_NAME = "i-do-not-exist";
        setupClient(WORKSPACE_NAME);
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
        setupClient(TEST_WORKSPACE);
        assertEquals("7", client.getLatestRevision(null).getRevisionString());
    }

    public void testGetLatestRevisionRestrictedToView() throws ScmException
    {
        setupClient(DEPOT_WORKSPACE);
        assertEquals("4", client.getLatestRevision(null).getRevisionString());
    }

    public void testGetRevisionsLatest() throws ScmException
    {
        setupClient(DEPOT_WORKSPACE);
        Revision latest = client.getLatestRevision(null);
        assertEquals(0, client.getRevisions(null, latest, null).size());
    }

    public void testGetRevisions() throws ScmException
    {
        setupClient(DEPOT_WORKSPACE);
        Revision latest = client.getLatestRevision(null);
        List<Revision> revisions = client.getRevisions(null, latest.calculatePreviousNumericalRevision(), null);
        assertEquals(1, revisions.size());
        assertEquals(latest, revisions.get(0));
    }

    public void testGetRevisionsBetweenLabels() throws ScmException
    {
        setupClient(TEST_WORKSPACE);
        List<Revision> revisions = client.getRevisions(null, new Revision(LABEL_TWO), new Revision(LABEL_LATEST));
        // The latest thing in our view is revision 7, so we expect revisions 3-7.
        assertEquals(5, revisions.size());
        assertEquals("3", revisions.get(0).getRevisionString());
        assertEquals("7", revisions.get(4).getRevisionString());
    }

    public void testCheckoutHead() throws Exception
    {
        setupClient(DEPOT_WORKSPACE);
        List<String> statuses = checkoutChanges(workDir, null, 4);

        assertEquals(11, statuses.size());
        for (int i = 1; i < 11; i++)
        {
            String status = statuses.get(i);

            // Foolish of me to create file10 which ruins lexical ordering :|
            int number;
            if (i == 1)
            {
                number = 1;
            }
            else if (i == 2)
            {
                number = 10;
            }
            else
            {
                number = i - 1;
            }

            String re = String.format("//depot/file%d#[1-9][0-9]* - added as.*", number);
            assertTrue("Status '" + status + "' does not match expected format '" + re + "'", status.matches(re));
        }

        checkDirectory("checkoutHead");
    }

    public void testCheckoutRevision() throws Exception
    {
        setupClient(DEPOT_WORKSPACE);

        Revision revision = client.checkout(createExecutionContext(workDir, false), createRevision(1), null);
        assertEquals("1", revision.getRevisionString());
        checkDirectory("checkoutRevision");
    }

    public void testCheckoutCustomView() throws ScmException, IOException
    {
        setupClient("//depot/... //pulse/nested/depot/...");
        
        Revision revision = client.checkout(createExecutionContext(workDir, false), createRevision(1), null);
        assertEquals("1", revision.getRevisionString());
        checkDirectory("checkoutRevision", "nested");
    }

    public void testCheckoutCustomMultilineView() throws ScmException, IOException
    {
        setupClient(
                "//depot/... //pulse/...\n" +
                "-//depot/file1 //pulse/file1");

        client.checkout(createExecutionContext(workDir, false), null, null);
        assertFalse(new File(workDir, "file1").exists());
        assertTrue(new File(workDir, "file2").exists());
    }

    public void testSyncWorkspacePersists() throws ScmException, IOException
    {
        setupClient(DEPOT_WORKSPACE);

        ExecutionContext context = createExecutionContext(workDir, false);
        client.checkout(context, createRevision(1), null);
        assertTrue(core.workspaceExists(PerforceWorkspaceManager.getSyncWorkspaceName(new PerforceConfiguration(), context)));
    }

    public void testSyncWorkspaceCleanedOnDestroy() throws ScmException, IOException
    {
        setupClient(DEPOT_WORKSPACE);

        ExecutionContext context = createExecutionContext(workDir, false);
        client.checkout(context, createRevision(1), null);
        String workspaceName = PerforceWorkspaceManager.getSyncWorkspaceName(new PerforceConfiguration(), context);
        assertTrue(core.workspaceExists(workspaceName));

        client.destroy(createScmContext(), new RecordingScmFeedbackHandler());
        assertFalse(core.workspaceExists(workspaceName));
    }

    public void testCheckoutFile() throws ScmException, IOException
    {
        setupClient(DEPOT_WORKSPACE);
        String content = IOUtils.inputStreamToString(client.retrieve(null, FileSystemUtils.composeFilename("depot", "file2"), null));
        assertEquals("content of file2: edited at the same time as file2 in depot2.\n", content);
    }

    public void testCheckoutFileRevision() throws ScmException, IOException
    {
        setupClient(DEPOT_WORKSPACE);
        String content = IOUtils.inputStreamToString(client.retrieve(null, FileSystemUtils.composeFilename("depot", "file2"), createRevision(2)));
        assertEquals("content of file2\n", content);
    }

    public void testProjectWorkspacePersists() throws ScmException
    {
        setupClient(DEPOT_WORKSPACE);

        client.retrieve(createScmContext(), FileSystemUtils.composeFilename("depot", "file2"), null);
        assertTrue(core.workspaceExists("pulse-" + TEST_PROJECT_HANDLE));
    }

    public void testProjectWorkspaceCleanedOnDestroy() throws ScmException
    {
        setupClient(DEPOT_WORKSPACE);

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
        setupClient(TEST_WORKSPACE);
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
        setupClient(TEST_WORKSPACE);
        List<Changelist> changes = client.getChanges(null, createRevision(7), createRevision(1));
        assertEquals(6, changes.size());
        Changelist list = changes.get(4);
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
        setupClient(DEPOT_WORKSPACE);
        List<Changelist> changes = client.getChanges(null, createRevision(1), createRevision(7));
        assertEquals(1, changes.size());
        assertEquals("4", (changes.get(0).getRevision()).getRevisionString());
    }

    public void testGetChangesFiltersAllFiles() throws Exception
    {
        setupClient(TEST_WORKSPACE, "//depot2/**");
        List<Changelist> changes = client.getChanges(null, createRevision(4), createRevision(5));
        assertEquals(0, changes.size());
    }

    public void testGetChangesFiltersSomeFiles() throws Exception
    {
        setupClient(TEST_WORKSPACE, "//depot2/test-branch/file1", "//depot2/test-branch/file5");
        List<Changelist> changes = client.getChanges(null, createRevision(4), createRevision(5));
        assertEquals(1, changes.size());
        assertEquals(7, changes.get(0).getChanges().size());
    }
    
    public void testGetChangesFilterLeavesNoFilesInView() throws Exception
    {
        setupClient(DEPOT_WORKSPACE, "//depot/**");
        List<Changelist> changes = client.getChanges(null, createRevision(1), createRevision(7));
        assertEquals(0, changes.size());
    }

    public void testListNonExistent() throws ScmException
    {
        setupClient(TEST_WORKSPACE);
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
        setupClient(TEST_WORKSPACE);
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
        setupClient(TEST_WORKSPACE);
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
        setupClient("complex-client");
        List<ScmFile> files = client.browse(null, "", null);
        assertEquals(1, files.size());
        ScmFile scmFile = files.get(0);
        assertEquals("src", scmFile.getName());
        assertTrue(scmFile.isDirectory());
    }

    public void testListComplexSrc() throws ScmException
    {
        setupClient("complex-client");
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
        setupClient("complex-client");
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
        setupClient(TEST_WORKSPACE);
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
        setupClient(TEST_WORKSPACE);
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
        setupClient(TEST_WORKSPACE);
        client.tag(null, null, createRevision(5), "test-tag", false);
        assertTrue(client.labelExists(TEST_WORKSPACE, "test-tag"));
        client.tag(null, null, createRevision(5), "test-tag", true);
    }

    public void testGetRevisionsSince() throws ScmException
    {
        setupClient(TEST_WORKSPACE);
        List<Revision> revisions = client.getRevisions(null, createRevision(5), null);
        assertEquals(2, revisions.size());
        assertEquals("6", revisions.get(0).getRevisionString());
        assertEquals("7", revisions.get(1).getRevisionString());
    }

    public void testGetRevisionsSinceLatest() throws ScmException
    {
        setupClient(TEST_WORKSPACE);
        List<Revision> revisions = client.getRevisions(null, createRevision(7), null);
        assertEquals(0, revisions.size());
    }

    public void testGetRevisionsInRange() throws ScmException
    {
        setupClient(TEST_WORKSPACE);
        List<Revision> revisions = client.getRevisions(null, createRevision(3), createRevision(7));
        assertEquals(4, revisions.size());
        assertEquals("4", revisions.get(0).getRevisionString());
        assertEquals("5", revisions.get(1).getRevisionString());
        assertEquals("6", revisions.get(2).getRevisionString());
        assertEquals("7", revisions.get(3).getRevisionString());
    }

    public void testGetRevisionsInReverseRange() throws ScmException
    {
        setupClient(TEST_WORKSPACE);
        List<Revision> revisions = client.getRevisions(null, createRevision(7), createRevision(3));
        assertEquals(4, revisions.size());
        assertEquals("7", revisions.get(0).getRevisionString());
        assertEquals("6", revisions.get(1).getRevisionString());
        assertEquals("5", revisions.get(2).getRevisionString());
        assertEquals("4", revisions.get(3).getRevisionString());
    }

    public void testGetRevisionsSinceFiltered() throws ScmException
    {
        setupClient(TEST_WORKSPACE, "//depot2/*");
        List<Revision> revisions = client.getRevisions(null, createRevision(5), null);
        assertEquals(1, revisions.size());
        assertEquals("7", revisions.get(0).getRevisionString());
    }

    public void testCheckoutThenUpdate() throws ScmException, IOException
    {
        setupClient(DEPOT_WORKSPACE);

        Revision got = client.checkout(createExecutionContext(workDir, true), createRevision(1), null);
        assertEquals("1", got.getRevisionString());
        checkDirectory("checkoutRevision");

        List<String> statuses = updateChanges(workDir, createRevision(8));
        checkDirectory("checkoutHead");
        assertEquals(2, statuses.size());
        assertTrue(statuses.get(1).startsWith("//depot/file2#2 - updating"));
    }

    public void testUpdateSameRevision() throws ScmException, IOException
    {
        setupClient(DEPOT_WORKSPACE);

        client.checkout(createExecutionContext(workDir, true), null, null);

        List<String> statuses = updateChanges(workDir, null);
        checkDirectory("checkoutHead");
        assertEquals(1, statuses.size());
    }

    public void testMultiUpdates() throws ScmException, IOException
    {
        setupClient(TEST_WORKSPACE);

        client.checkout(createExecutionContext(workDir, true), createRevision(1), null);

        for(int i = 2; i <= 8; i++)
        {
            Revision updateRevision = createRevision(i);
            updateChanges(workDir, updateRevision);
        }
    }

    public void testParseRevisionLatest() throws ScmException
    {
        setupClient(TEST_WORKSPACE);
        Revision latest = client.getLatestRevision(null);
        Revision rev = client.parseRevision(null, latest.getRevisionString());
        assertEquals(latest.getRevisionString(), rev.getRevisionString());
    }

    public void testParseRevisionLabel() throws ScmException
    {
        setupClient(TEST_WORKSPACE);
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
        setupClient(TEST_WORKSPACE);
        Revision rev = client.parseRevision(null, "10000");
        assertEquals(client.getLatestRevision(null).toString(), rev.getRevisionString());
    }

    public void testParseRevisionInvalid() throws ScmException
    {
        failedParseRevisionHelper("bullet", "Invalid changelist/client/label/date");
    }

    private void failedParseRevisionHelper(String revision, String expectedMessage) throws ScmException
    {
        setupClient(TEST_WORKSPACE);
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

        PerforceCore core = getCore();
        core.createOrUpdateWorkspace(TEST_WORKSPACE, "unlocked-client", "description", tmpDir.getAbsolutePath(), null, null);
        spec = SystemUtils.runCommand("p4", "-p", "6666", "client", "-o", TEST_WORKSPACE);
        assertTrue(spec.contains(" locked"));
        spec = SystemUtils.runCommand("p4", "-p", "6666", "client", "-o", "unlocked-client");
        assertFalse(spec.contains(" locked"));
    }

    public void testDeletedChangelist() throws Exception
    {
        // CIB-1010
        setupClient(TEST_WORKSPACE, "//depot/file2");

        PerforceCore core = getCore();
        core.runP4("Change: new\n" +
                     "Client: test-client\n" +
                     "User: test-user\n" +
                     "Status: new\n" +
                     "Description:\n" +
                     "    Dead changelist",
                     "p4", FLAG_CLIENT, TEST_WORKSPACE, "change", "-i");
        core.runP4(null, "p4", FLAG_CLIENT, TEST_WORKSPACE, "change", "-d", "9");

        core.createOrUpdateWorkspace(TEST_WORKSPACE, "edit-client", "description", workDir.getAbsolutePath(), null, null);
        core.setEnv(PerforceConstants.ENV_CLIENT, "edit-client");
        core.runP4(null, "p4", "sync");
        core.runP4(null, "p4", "edit", "//depot/file1");
        core.submit("test edit");
        core.runP4(null, "p4", "client", "-d", "edit-client");

        assertTrue(client.getRevisions(null, new Revision(7), null).size() > 0);
    }

    public void testGetEmail() throws ScmException
    {
        setupClient(TEST_WORKSPACE);
        assertEquals("jsankey@bob", client.getEmailAddress(null, "jsankey"));
    }

    public void testBootstrapFeedback() throws ScmException
    {
        setupClient(TEST_WORKSPACE);
        ExecutionContext context = createExecutionContext(workDir, true);

        RecordingScmFeedbackHandler handler = new RecordingScmFeedbackHandler();
        client.checkout(context, new Revision("1"), handler);
        
        String expectedWorkspace = PerforceWorkspaceManager.getSyncWorkspaceName(client.getConfiguration(), context);
        assertEquals(">> p4 -c " + expectedWorkspace + " sync -f @1", handler.getStatusMessages().get(0));
    }

    public void testErrorIncludesCommandLine() throws ScmException, IOException
    {
        PerforceConfiguration configuration = new PerforceConfiguration(getP4Port(), "invalid", "", DEPOT_WORKSPACE);
        client = new PerforceClient(configuration, new PerforceWorkspaceManager());
        try
        {
            client.testConnection();
            fail("Connection test should fail with bad user");
        }
        catch (ScmException e)
        {
            assertThat(e.getMessage(), containsString("'p4 -c depot-client client -o' returned non-zero exit code"));
        }
    }
    
    private PerforceCore getCore()
    {
        PerforceCore core = new PerforceCore();
        core.setEnv(PerforceConstants.ENV_PORT, getP4Port());
        core.setEnv(PerforceConstants.ENV_USER, "test-user");
        return core;
    }

    private void setupClient(String workspaceOrView, String... excludedPaths) throws ScmException
    {
        PerforceConfiguration configuration;
        if (workspaceOrView.contains("//"))
        {
            configuration = new PerforceConfiguration(getP4Port(), "test-user", "", null);
            configuration.setUseTemplateClient(false);
            configuration.setView(workspaceOrView);
        }
        else
        {
            configuration = new PerforceConfiguration(getP4Port(), "test-user", "", workspaceOrView);
        }
        
        configuration.setFilterPaths(Arrays.asList(excludedPaths));
        this.client = new PerforceClient(configuration, new PerforceWorkspaceManager());
        this.core.setEnv(PerforceConstants.ENV_PORT, getP4Port());
        this.core.setEnv(PerforceConstants.ENV_USER, "test-user");
    }

    private void checkDirectory(String name) throws IOException
    {
        checkDirectory(name, null);
    }

    private void checkDirectory(String name, String nestedUnder) throws IOException
    {
        File expectedDir = new File(tmpDir, DIR_EXPECTED);
        removeDirectory(expectedDir);
        assertTrue(expectedDir.mkdirs());
        unzipInput(name, expectedDir);

        File dir;
        if (nestedUnder == null)
        {
            dir = workDir;
        }
        else
        {
            dir = new File(workDir, nestedUnder);
        }

        IOAssertions.assertDirectoriesEqual(expectedDir, dir);
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
