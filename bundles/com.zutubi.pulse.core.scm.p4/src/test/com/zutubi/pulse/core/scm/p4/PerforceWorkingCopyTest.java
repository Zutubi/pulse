package com.zutubi.pulse.core.scm.p4;

import com.zutubi.diff.PatchFile;
import com.zutubi.diff.PatchFileParser;
import com.zutubi.diff.PatchParseException;
import com.zutubi.diff.unified.UnifiedHunk;
import com.zutubi.diff.unified.UnifiedPatch;
import com.zutubi.diff.unified.UnifiedPatchParser;
import com.zutubi.pulse.core.scm.WorkingCopyContextImpl;
import com.zutubi.pulse.core.scm.api.EOLStyle;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.api.WorkingCopyContext;
import com.zutubi.pulse.core.scm.patch.api.FileStatus;
import com.zutubi.pulse.core.scm.patch.api.WorkingCopyStatus;
import com.zutubi.pulse.core.ui.TestUI;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.config.PropertiesConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import static com.zutubi.pulse.core.scm.p4.PerforceConstants.*;

public class PerforceWorkingCopyTest extends PerforceTestBase
{
    private static final long REVISION_LATEST = 2;

    private static final String P4_COMMAND = "p4";
    private static final String CLIENT_NAME = "test-client-1";
    private static final String OTHER_CLIENT_NAME = "test-client-2";

    private File clientRoot;
    private PerforceCore core;
    private File otherClientRoot;
    private PerforceCore otherCore;
    private WorkingCopyContext context;

    private PerforceWorkingCopy wc;
    private TestUI ui;

    protected void setUp() throws Exception
    {
        super.setUp();

        deployPerforceServer("repo", P4D_PORT, 1, false);

        createClients();

        wc = new PerforceWorkingCopy();

        PropertiesConfig config = new PropertiesConfig();
        config.setProperty(PerforceWorkingCopy.PROPERTY_CLIENT, CLIENT_NAME);
        config.setProperty(PerforceWorkingCopy.PROPERTY_PORT, getP4Port());
        config.setProperty(PerforceWorkingCopy.PROPERTY_USER, "test-user");

        ui = new TestUI();
        context = new WorkingCopyContextImpl(clientRoot, config, ui);
    }

    private void createClients() throws ScmException
    {
        clientRoot = new File(tmpDir, CLIENT_NAME);
        assertTrue(clientRoot.mkdir());

        core = new PerforceCore();
        core.setEnv(ENV_CLIENT, CLIENT_NAME);
        core.setEnv(ENV_PORT, getP4Port());
        core.setEnv(ENV_USER, "test-user");
        core.setEnv("PWD", clientRoot.getAbsolutePath());
        core.setWorkingDir(clientRoot);

        core.createOrUpdateWorkspace("client-1", CLIENT_NAME, "description", clientRoot.getAbsolutePath(), null, null);
        core.runP4(null, P4_COMMAND, COMMAND_SYNC, FLAG_FORCE);

        otherClientRoot = new File(tmpDir, OTHER_CLIENT_NAME);
        assertTrue(otherClientRoot.mkdir());

        otherCore = new PerforceCore();
        otherCore.setEnv(ENV_CLIENT, OTHER_CLIENT_NAME);
        otherCore.setEnv(ENV_PORT, getP4Port());
        otherCore.setEnv(ENV_USER, "test-user");
        otherCore.setEnv("PWD", otherClientRoot.getAbsolutePath());
        otherCore.setWorkingDir(otherClientRoot);

        otherCore.createOrUpdateWorkspace("client-1", OTHER_CLIENT_NAME, "description", otherClientRoot.getAbsolutePath(), null, null);
        otherCore.runP4(null, P4_COMMAND, COMMAND_SYNC, FLAG_FORCE);
    }

    public void testLocationMatches() throws ScmException
    {
        assertTrue(wc.matchesLocation(context, "foo@" + getP4Port()));
    }

    public void testLocationDoesntMatch() throws ScmException
    {
        assertFalse(wc.matchesLocation(context, "foo@anotherhost:6666"));
        assertWarning("P4PORT setting ':6666' does not match Pulse project's P4PORT 'anotherhost:6666'");
    }

    public void testLocalStatusNoChanges() throws ScmException
    {
        WorkingCopyStatus status = wc.getLocalStatus(context);
        assertEquals(0, status.getFileStatuses().size());
    }

    public void testLocalStatusOpenForEdit() throws ScmException
    {
        openForEdit("file1");

        WorkingCopyStatus status = wc.getLocalStatus(context);
        assertChange(status, "file1", FileStatus.State.MODIFIED, true);
    }

    public void testLocalStatusOpenForEditExecutable() throws ScmException
    {
        core.runP4(null, P4_COMMAND, COMMAND_EDIT, "script1");

        WorkingCopyStatus status = wc.getLocalStatus(context);
        assertChange(status, "script1", FileStatus.State.MODIFIED, true);
    }

    public void testLocalStatusOpenForEditAddExecutable() throws ScmException
    {
        core.runP4(null, P4_COMMAND, COMMAND_EDIT, FLAG_TYPE, "text+x", "file1");

        WorkingCopyStatus status = wc.getLocalStatus(context);
        assertExecutable(status, "file1", true);
    }

    public void testLocalStatusOpenForEditRemoveExecutable() throws ScmException
    {
        core.runP4(null, P4_COMMAND, COMMAND_EDIT, FLAG_TYPE, "text", "script1");

        WorkingCopyStatus status = wc.getLocalStatus(context);
        assertExecutable(status, "script1", false);
    }

    public void testLocalStatusOpenForEditBinary() throws ScmException
    {
        core.runP4(null, P4_COMMAND, COMMAND_EDIT, "bin1");

        WorkingCopyStatus status = wc.getLocalStatus(context);
        assertChange(status, "bin1", FileStatus.State.MODIFIED, false);
    }

    public void testLocalStatusOpenForEditAddBinary() throws ScmException
    {
        core.runP4(null, P4_COMMAND, COMMAND_EDIT, FLAG_TYPE, "binary", "file1");

        WorkingCopyStatus status = wc.getLocalStatus(context);
        assertChange(status, "file1", FileStatus.State.MODIFIED, false);
    }

    public void testLocalStatusOpenForEditRemoveBinary() throws ScmException
    {
        core.runP4(null, P4_COMMAND, COMMAND_EDIT, FLAG_TYPE, "text", "bin1");

        WorkingCopyStatus status = wc.getLocalStatus(context);
        assertChange(status, "bin1", FileStatus.State.MODIFIED, true);
    }

    public void testLocalStatusOpenForAdd() throws ScmException, IOException
    {
        openForAdd(null);

        WorkingCopyStatus status = wc.getLocalStatus(context);
        assertChange(status, "newfile", FileStatus.State.ADDED, true);
    }

    public void testLocalStatusOpenForAddBinary() throws ScmException, IOException
    {
        openForAdd("binary");

        WorkingCopyStatus status = wc.getLocalStatus(context);
        assertChange(status, "newfile", FileStatus.State.ADDED, false);
    }

    public void testLocalStatusOpenForAddExecutable() throws ScmException, IOException
    {
        openForAdd("text+x");

        WorkingCopyStatus status = wc.getLocalStatus(context);
        assertExecutable(status, "newfile", true);
    }

    public void testLocalStatusOpenForDelete() throws ScmException, IOException
    {
        core.runP4(null, P4_COMMAND, COMMAND_DELETE, "dir1/file1");

        WorkingCopyStatus status = wc.getLocalStatus(context);
        assertChange(status, "dir1/file1", FileStatus.State.DELETED, false);
    }

    public void testLocalStatusOpenForIntegrate() throws ScmException, IOException
    {
        core.runP4(null, P4_COMMAND, COMMAND_INTEGRATE, "file1", "integrated");

        WorkingCopyStatus status = wc.getLocalStatus(context);
        assertChange(status, "integrated", FileStatus.State.BRANCHED, true);
    }

    public void testLocalStatusOpenForIntegrateEdited() throws ScmException, IOException
    {
        openForMerge("file1");

        WorkingCopyStatus status = wc.getLocalStatus(context);
        assertChange(status, "file1", FileStatus.State.MERGED, true);
    }

    public void testLocalStatusOpenForIntegrateBinary() throws ScmException, IOException
    {
        openForMerge("bin1");

        WorkingCopyStatus status = wc.getLocalStatus(context);
        assertChange(status, "bin1", FileStatus.State.MERGED, false);
    }

    public void testLocalStatusOpenForIntegrateExecutable() throws ScmException, IOException
    {
        openForMerge("script1");
        
        WorkingCopyStatus status = wc.getLocalStatus(context);
        assertChange(status, "script1", FileStatus.State.MERGED, true);
    }

    public void testLocalStatusOpenForIntegrateAddExecutable() throws ScmException, IOException
    {
        openForMerge("file1", "text+x");

        WorkingCopyStatus status = wc.getLocalStatus(context);
        assertExecutable(status, "file1", true);
    }

    public void testLocalStatusOpenForIntegrateRemoveExecutable() throws ScmException, IOException
    {
        openForMerge("script1", "text");

        WorkingCopyStatus status = wc.getLocalStatus(context);
        assertExecutable(status, "script1", false);
    }

    public void testLocalStatusIgnoresOOD() throws ScmException, IOException
    {
        otherEdit();

        WorkingCopyStatus status = wc.getLocalStatus(context);
        assertEquals(0, status.getFileStatuses().size());
    }

    public void testLocalStatusOODAndLocalChange() throws ScmException
    {
        openForEdit("file1");

        otherDelete("file1");

        WorkingCopyStatus status = wc.getLocalStatus(context);
        assertChange(status, "file1", FileStatus.State.MODIFIED, true);
    }

    public void testLocalStatusRestrictedToFiles() throws ScmException
    {
        openForEdit("bin1");
        openForEdit("file1");
        openForEdit("script1");

        WorkingCopyStatus status = wc.getLocalStatus(context, "//depot/bin1", "//depot/script1");
        assertEquals(2, status.getFileStatuses().size());
        assertEquals("bin1", status.getFileStatuses().get(0).getPath());
        assertEquals("script1", status.getFileStatuses().get(1).getPath());
    }

    public void testLocalStatusRestrictedToUnchangedFiles() throws ScmException
    {
        openForEdit("file1");

        WorkingCopyStatus status = wc.getLocalStatus(context, "//depot/bin1", "//depot/script1");
        assertEquals(0, status.getFileStatuses().size());
    }

    public void testLocalStatusDefaultChangelist() throws ScmException
    {
        openForEdit("bin1");
        openForEdit("file1");

        WorkingCopyStatus status = wc.getLocalStatus(context, ":default");
        assertEquals(2, status.getFileStatuses().size());
        assertEquals("bin1", status.getFileStatuses().get(0).getPath());
        assertEquals("file1", status.getFileStatuses().get(1).getPath());
    }

    public void testLocalStatusChangelist() throws ScmException
    {
        long changelist = core.createChangelist("test");
        openForEdit("bin1", changelist);
        openForEdit("file1");
        openForEdit("script1", changelist);

        WorkingCopyStatus status = wc.getLocalStatus(context, ":" + Long.toString(changelist));
        assertEquals(2, status.getFileStatuses().size());
        assertEquals("bin1", status.getFileStatuses().get(0).getPath());
        assertEquals("script1", status.getFileStatuses().get(1).getPath());
    }

    public void testLocalStatusChangelistNoChanges() throws ScmException
    {
        long changelist = core.createChangelist("test");
        openForEdit("file1");

        WorkingCopyStatus status = wc.getLocalStatus(context, ":" + Long.toString(changelist));
        assertEquals(0, status.getFileStatuses().size());
    }

    public void testLocalStatusCompatibleSpecifyChangelist() throws ScmException
    {
        context.getConfig().setProperty(PerforceWorkingCopy.PROPERTY_PRE_2004_2, "true");
        try
        {
            wc.getLocalStatus(context, ":q");
            fail("Should not be able to specify changelist when using compatibility option");
        }
        catch (ScmException e)
        {
            assertTrue(e.getMessage().contains("Unable to specify a changelist"));
        }
    }

    public void testUpdateAlreadyUpToDate() throws ScmException
    {
        wc.update(context, Revision.HEAD);
        WorkingCopyStatus status = wc.getLocalStatus(context);
        assertEquals(0, status.getFileStatuses().size());
    }

    public void testSimpleUpdate() throws ScmException, IOException
    {
        otherEdit();

        wc.update(context, Revision.HEAD);
        WorkingCopyStatus status = wc.getLocalStatus(context);
        assertEquals(0, status.getFileStatuses().size());
    }

    public void testUpdateMerge() throws ScmException, IOException
    {
        otherEdit();
        openForEdit("file1");

        wc.update(context, Revision.HEAD);
        WorkingCopyStatus status = wc.getLocalStatus(context);
        assertChange(status, "file1", FileStatus.State.MODIFIED, true);
    }

    public void testUpdateConflict() throws ScmException, IOException
    {
        otherEdit();

        openForEdit("file1");
        File file1 = new File(clientRoot, "file1");
        FileSystemUtils.createFile(file1, "this");

        wc.update(context, Revision.HEAD);
        WorkingCopyStatus status = wc.getLocalStatus(context);
        assertChange(status, "file1", FileStatus.State.UNRESOLVED, false);
    }

    public void testCanDiffText() throws ScmException
    {
        assertTrue(wc.canDiff(context, "file1"));
    }

    public void testCanDiffTextOpenForEdit() throws ScmException
    {
        openForEdit("file1");
        assertTrue(wc.canDiff(context, "file1"));
    }

    public void testCanDiffBinary() throws ScmException
    {
        assertFalse(wc.canDiff(context, "bin1"));
    }

    public void testCanDiffBinaryOpenForEdit() throws ScmException
    {
        openForEdit("bin1");
        assertFalse(wc.canDiff(context, "bin1"));
    }

    public void testDiffTextFile() throws ScmException, PatchParseException, IOException
    {
        openForEdit("file1");
        FileSystemUtils.createFile(new File(clientRoot, "file1"), "a line\n");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        wc.diff(context, "file1", baos);

        String diff = new String(baos.toByteArray());
        PatchFileParser parser = new PatchFileParser(new UnifiedPatchParser());
        StringReader reader = new StringReader(diff);
        PatchFile pf = parser.parse(reader);
        assertEquals(1, pf.getPatches().size());

        UnifiedPatch patch = (UnifiedPatch) pf.getPatches().get(0);
        assertEquals(1, patch.getHunks().size());

        UnifiedHunk hunk = patch.getHunks().get(0);
        assertEquals(1, hunk.getLines().size());

        UnifiedHunk.Line line = hunk.getLines().get(0);
        assertEquals("a line", line.getContent());
        assertEquals(UnifiedHunk.LineType.ADDED, line.getType());
    }
    
    public void testCanDiffContextBaseNotSameAsClientRoot() throws ScmException
    {
        context = new WorkingCopyContextImpl(new File(context.getBase(), "subdir"), context.getConfig(), context.getUI());
        assertTrue(wc.canDiff(context, "file1"));
    }

    public void testGetLatestRemoteRevision() throws ScmException
    {
        assertEquals(Long.toString(REVISION_LATEST), wc.getLatestRemoteRevision(context).getRevisionString());
    }

    public void testGetLatestRemoteRevisionPendingChange() throws ScmException
    {
        openForEdit("file1");
        assertEquals(Long.toString(REVISION_LATEST), wc.getLatestRemoteRevision(context).getRevisionString());
    }

    public void testGetLatestRemoteRevisionRestrictedToView() throws ScmException, IOException
    {
        excludeBranchesFromClient();
        assertEquals("1", wc.getLatestRemoteRevision(context).getRevisionString());
    }

    public void testGetLatestRemoteRevisionSeesNewChanges() throws ScmException, IOException
    {
        openForEdit("file1");
        core.submit("trivial edit");
        assertEquals(Long.toString(REVISION_LATEST + 1), wc.getLatestRemoteRevision(context).getRevisionString());
    }

    public void testGuessLocalRevisionUpToDate() throws ScmException
    {
        assertEquals(Long.toString(REVISION_LATEST), wc.guessLocalRevision(context).getRevisionString());
    }

    public void testGuessLocalRevisionPendingChange() throws ScmException
    {
        openForEdit("file1");
        assertEquals(Long.toString(REVISION_LATEST), wc.guessLocalRevision(context).getRevisionString());
    }

    public void testGuessLocalRevisionRestrictedToView() throws ScmException, IOException
    {
        excludeBranchesFromClient();
        core.runP4(null, P4_COMMAND, COMMAND_SYNC);
        assertEquals("1", wc.guessLocalRevision(context).getRevisionString());
    }

    public void testGuessLocalRevisionOutOfDate() throws ScmException, IOException
    {
        otherEdit();
        assertEquals(Long.toString(REVISION_LATEST), wc.guessLocalRevision(context).getRevisionString());
    }

    public void testGuessLocalRevisionLatestCommitFromWC() throws ScmException, IOException
    {
        openForEdit("file1");
        core.submit("trivial edit");
        assertEquals(Long.toString(REVISION_LATEST + 1), wc.guessLocalRevision(context).getRevisionString());
    }

    public void testGuessLocalRevisionMixedRevisions() throws ScmException, IOException
    {
        otherEditFiles("file1", "file2");
        core.runP4(null, P4_COMMAND, COMMAND_SYNC, "file1");
        try
        {
            wc.guessLocalRevision(context).getRevisionString();
            fail();
        }
        catch (ScmException e)
        {
            assertEquals("Unable to guess have revision: tried [3]: is your client at a single changelist?", e.getMessage());
        }
    }

    public void testGuessLocalRevisionLatestAreDeletes() throws ScmException, IOException
    {
        otherDelete("file1");
        otherDelete("file2");
        core.runP4(null, P4_COMMAND, COMMAND_SYNC);
        assertEquals(Long.toString(REVISION_LATEST + 2), wc.guessLocalRevision(context).getRevisionString());
    }

    public void testGuessLocalRevisionLatestAreDeletesSpreadChangelistNumbers() throws ScmException, IOException
    {
        final String EXTERNAL_FILE = "branches/1/file1";

        // Simulate developers on other projects checking in to bump up
        // changelist between our delete changelists.
        excludeBranchesFromClient();
        otherEditFiles(EXTERNAL_FILE);
        otherEditFiles(EXTERNAL_FILE);
        otherEditFiles(EXTERNAL_FILE);
        otherEditFiles(EXTERNAL_FILE);
        otherEditFiles(EXTERNAL_FILE);
        otherEditFiles(EXTERNAL_FILE);
        otherDelete("file1");
        otherEditFiles(EXTERNAL_FILE);
        otherEditFiles(EXTERNAL_FILE);
        otherDelete("file2");
        otherEditFiles(EXTERNAL_FILE);
        otherEditFiles(EXTERNAL_FILE);
        otherEditFiles(EXTERNAL_FILE);
        otherEditFiles(EXTERNAL_FILE);
        otherDelete("file3"); // 15th change is last to affect our project
        otherEditFiles(EXTERNAL_FILE);
        otherEditFiles(EXTERNAL_FILE);

        core.runP4(null, P4_COMMAND, COMMAND_SYNC);
        assertEquals(Long.toString(REVISION_LATEST + 15), wc.guessLocalRevision(context).getRevisionString());
    }

    private void excludeBranchesFromClient() throws ScmException
    {
        PerforceWorkspace workspace = new PerforceWorkspace(CLIENT_NAME, clientRoot.getAbsolutePath(), Arrays.asList("//depot/... //" + CLIENT_NAME + "/...", "-//depot/branches/... //" + CLIENT_NAME + "/branches/..."));
        core.runP4(workspace.toSpecification(), P4_COMMAND, COMMAND_CLIENT, FLAG_INPUT);
    }

    private void openForEdit(String path) throws ScmException
    {
        core.runP4(null, P4_COMMAND, COMMAND_EDIT, path);
    }

    private void openForEdit(String path, long changelist) throws ScmException
    {
        core.runP4(null, P4_COMMAND, COMMAND_EDIT, FLAG_CHANGELIST, Long.toString(changelist), path);
    }

    private void openForAdd(String type) throws IOException, ScmException
    {
        File newFile = new File(clientRoot, "newfile");
        FileSystemUtils.createFile(newFile, "new");
        if(type == null)
        {
            core.runP4(null, P4_COMMAND, COMMAND_ADD, "newfile");
        }
        else
        {
            core.runP4(null, P4_COMMAND, COMMAND_ADD, FLAG_TYPE, type, "newfile");
        }
    }

    private void openForMerge(String path) throws ScmException
    {
        openForMerge(path, null);
    }

    private void openForMerge(String path, String type) throws ScmException
    {
        if(type == null)
        {
            core.runP4(null, P4_COMMAND, COMMAND_EDIT, "branches/1/" + path);
        }
        else
        {
            core.runP4(null, P4_COMMAND, COMMAND_EDIT, FLAG_TYPE, type, "branches/1/" + path);
        }
        
        core.submit("edit on branch 1");

        if(type == null)
        {
            core.runP4(null, P4_COMMAND, COMMAND_INTEGRATE, "branches/1/...", "...");
        }
        else
        {
            core.runP4(null, P4_COMMAND, COMMAND_INTEGRATE, FLAG_TYPE, "branches/1/...", "...");
        }
        
        core.runP4(null, P4_COMMAND, COMMAND_RESOLVE, FLAG_AUTO_MERGE);
    }

    private void otherEdit() throws ScmException, IOException
    {
        otherEditFiles("file1");
    }

    private void otherEditFiles(String... paths) throws ScmException, IOException
    {
        for (String path: paths)
        {
            otherCore.runP4(null, P4_COMMAND, COMMAND_EDIT, path);
            File other1 = new File(otherClientRoot, path);
            FileSystemUtils.createFile(other1, "edited by " + getName());
        }

        otherCore.submit("comment");
    }

    private void otherDelete(String path) throws ScmException
    {
        otherCore.runP4(null, P4_COMMAND, COMMAND_DELETE, path);
        otherCore.submit("comment");
    }

    private void assertChange(WorkingCopyStatus status, String path, FileStatus.State state, boolean text)
    {
        List<FileStatus> changes = status.getFileStatuses();
        assertEquals(1, changes.size());
        FileStatus fs = changes.get(0);
        assertEquals(path, fs.getPath());
        assertEquals(state, fs.getState());
        assertFalse(fs.isDirectory());
        if(text)
        {
            assertEquals(1, fs.getProperties().size());
            assertEquals(EOLStyle.TEXT.toString(), fs.getProperty(FileStatus.PROPERTY_EOL_STYLE));
        }
        else
        {
            assertEquals(0, fs.getProperties().size());
        }
    }

    private void assertExecutable(WorkingCopyStatus status, String path, boolean executable)
    {
        FileStatus fs = status.getFileStatus(path);
        assertEquals(path, fs.getPath());
        assertEquals(Boolean.toString(executable), fs.getProperty(FileStatus.PROPERTY_EXECUTABLE));
    }


    private void assertWarning(String message)
    {
        assertEquals(message, ui.getWarningMessages().remove(0));
    }
}
