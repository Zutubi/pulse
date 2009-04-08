package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.personal.TestPersonalBuildUI;
import com.zutubi.pulse.core.scm.WorkingCopyContextImpl;
import com.zutubi.pulse.core.scm.api.*;
import static com.zutubi.pulse.core.scm.p4.PerforceConstants.*;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.config.PropertiesConfig;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PerforceWorkingCopyTest extends PerforceTestBase
{
    private static final String FILE_CHECKPOINT = "checkpoint.1";

    private static final String P4_COMMAND = "p4";
    private static final String CLIENT_NAME = "test-client-1";
    private static final String OTHER_CLIENT_NAME = "test-client-2";

    private File clientRoot;
    private PerforceCore core;
    private File otherClientRoot;
    private PerforceCore otherCore;
    private WorkingCopyContext context;

    private PerforceWorkingCopy wc;
    private TestPersonalBuildUI ui;

    protected String getCheckpointFilename()
    {
        return FILE_CHECKPOINT;
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        createClients();

        wc = new PerforceWorkingCopy();

        PropertiesConfig config = new PropertiesConfig();
        config.setProperty(PerforceWorkingCopy.PROPERTY_CLIENT, CLIENT_NAME);
        config.setProperty(PerforceWorkingCopy.PROPERTY_PORT, getP4Port());
        config.setProperty(PerforceWorkingCopy.PROPERTY_USER, "test-user");

        ui = new TestPersonalBuildUI();
        context = new WorkingCopyContextImpl(clientRoot, config, ui);
    }

    private void createClients() throws ScmException
    {
        clientRoot = new File(getTempDir(), CLIENT_NAME);
        assertTrue(clientRoot.mkdir());

        core = new PerforceCore();
        core.setEnv(ENV_CLIENT, CLIENT_NAME);
        core.setEnv(ENV_PORT, getP4Port());
        core.setEnv(ENV_USER, "test-user");
        core.setEnv("PWD", clientRoot.getAbsolutePath());
        core.setWorkingDir(clientRoot);

        core.createOrUpdateWorkspace("client-1", CLIENT_NAME, "description", clientRoot.getAbsolutePath());
        core.runP4(null, P4_COMMAND, COMMAND_SYNC, FLAG_FORCE);

        otherClientRoot = new File(getTempDir(), OTHER_CLIENT_NAME);
        assertTrue(otherClientRoot.mkdir());

        otherCore = new PerforceCore();
        otherCore.setEnv(ENV_CLIENT, OTHER_CLIENT_NAME);
        otherCore.setEnv(ENV_PORT, getP4Port());
        otherCore.setEnv(ENV_USER, "test-user");
        otherCore.setEnv("PWD", otherClientRoot.getAbsolutePath());
        otherCore.setWorkingDir(otherClientRoot);

        otherCore.createOrUpdateWorkspace("client-1", OTHER_CLIENT_NAME, "description", otherClientRoot.getAbsolutePath());
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

        otherCore.runP4(null, P4_COMMAND, COMMAND_DELETE, "file1");
        otherCore.submit("comment");

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
        otherCore.runP4(null, P4_COMMAND, COMMAND_EDIT, "file1");
        File other1 = new File(otherClientRoot, "file1");
        FileSystemUtils.createFile(other1, "other");
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
