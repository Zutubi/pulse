package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.scm.api.*;
import static com.zutubi.pulse.core.scm.p4.PerforceConstants.*;
import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.pulse.core.util.ZipUtils;
import com.zutubi.pulse.core.util.config.PropertiesConfig;
import com.zutubi.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class PerforceWorkingCopyTest extends PulseTestCase implements PersonalBuildUI
{
    private static final String P4_COMMAND = "p4";
    private static final String CLIENT_NAME = "test-client-1";
    private static final String OTHER_CLIENT_NAME = "test-client-2";
    private static final String HEAD_REVISION = "2";

    private File tempDir;
    private Process p4dProcess;
    private File clientRoot;
    private PerforceCore core;
    private File otherClientRoot;
    private PerforceCore otherCore;

    private List<String> statuses = new LinkedList<String>();
    private List<String> errors = new LinkedList<String>();
    private List<String> warnings = new LinkedList<String>();

    private PerforceWorkingCopy wc;

    protected void setUp() throws Exception
    {
        tempDir = FileSystemUtils.createTempDir(PerforceWorkingCopyTest.class.getName(), "");
        tempDir = tempDir.getCanonicalFile();
        File repoDir = new File(tempDir, "repo");
        repoDir.mkdir();

        File repoZip = getTestDataFile("bundles/com.zutubi.pulse.core.scm.p4", "repo", "zip");
        ZipUtils.extractZip(repoZip, repoDir);

        // Restore from checkpoint
        p4dProcess = Runtime.getRuntime().exec(new String[] { "p4d", "-r", repoDir.getAbsolutePath(), "-jr", "checkpoint.1"});
        p4dProcess.waitFor();
        
        p4dProcess = Runtime.getRuntime().exec(new String[] { "p4d", "-r", repoDir.getAbsolutePath()});
        waitForServer(1666);

        createClients();

        wc = new PerforceWorkingCopy(null, new PropertiesConfig());
        wc.setUI(this);
        wc.getClient().setEnv(ENV_CLIENT, CLIENT_NAME);
        wc.getClient().setEnv(ENV_PORT, ":1666");
        wc.getClient().setEnv(ENV_USER, "test-user");
        wc.getClient().setWorkingDir(clientRoot);
    }

    private void createClients() throws ScmException
    {
        clientRoot = new File(tempDir, CLIENT_NAME);
        clientRoot.mkdir();

        core = new PerforceCore();
        core.setEnv(ENV_CLIENT, CLIENT_NAME);
        core.setEnv(ENV_PORT, ":1666");
        core.setEnv(ENV_USER, "test-user");
        core.setEnv("PWD", clientRoot.getAbsolutePath());
        core.setWorkingDir(clientRoot);

        core.createClient("client-1", CLIENT_NAME, clientRoot);
        core.runP4(null, P4_COMMAND, COMMAND_SYNC, FLAG_FORCE);

        otherClientRoot = new File(tempDir, OTHER_CLIENT_NAME);
        otherClientRoot.mkdir();

        otherCore = new PerforceCore();
        otherCore.setEnv(ENV_CLIENT, OTHER_CLIENT_NAME);
        otherCore.setEnv(ENV_PORT, ":1666");
        otherCore.setEnv(ENV_USER, "test-user");
        otherCore.setEnv("PWD", otherClientRoot.getAbsolutePath());
        otherCore.setWorkingDir(otherClientRoot);

        otherCore.createClient("client-1", OTHER_CLIENT_NAME, otherClientRoot);
        otherCore.runP4(null, P4_COMMAND, COMMAND_SYNC, FLAG_FORCE);
    }

    protected void tearDown() throws Exception
    {
        p4dProcess.destroy();
        p4dProcess.waitFor();
        p4dProcess = null;
        Thread.sleep(100);

        FileSystemUtils.rmdir(tempDir);

        statuses = null;
        errors = null;
        warnings = null;
    }

    public void testLocationMatches() throws ScmException
    {
        assertTrue(wc.matchesLocation("foo@:1666"));
    }

    public void testLocationDoesntMatch() throws ScmException
    {
        assertFalse(wc.matchesLocation("foo@anotherhost:1666"));
        assertWarning("P4PORT setting ':1666' does not match Pulse project's P4PORT 'anotherhost:1666'");
    }

    public void testStatusNoChanges() throws ScmException
    {
        WorkingCopyStatus status = wc.getStatus();
        assertEquals(HEAD_REVISION, status.getRevision().getRevisionString());
        assertEquals(0, status.getChanges().size());
    }

    public void testLocalStatusNoChanges() throws ScmException
    {
        WorkingCopyStatus status = wc.getLocalStatus();
        assertEquals(0, status.getChanges().size());
    }

    public void testStatusOpenForEdit() throws ScmException
    {
        openForEdit("file1");

        WorkingCopyStatus status = wc.getStatus();
        assertEquals(HEAD_REVISION, status.getRevision().getRevisionString());
        assertChange(status, "file1", FileStatus.State.MODIFIED, false, true);
    }

    public void testLocalStatusOpenForEdit() throws ScmException
    {
        openForEdit("file1");

        WorkingCopyStatus status = wc.getLocalStatus();
        assertChange(status, "file1", FileStatus.State.MODIFIED, false, true);
    }

    public void testStatusOpenForEditExecutable() throws ScmException
    {
        core.runP4(null, P4_COMMAND, COMMAND_EDIT, "script1");

        WorkingCopyStatus status = wc.getStatus();
        assertEquals(HEAD_REVISION, status.getRevision().getRevisionString());
        assertChange(status, "script1", FileStatus.State.MODIFIED, false, true);
    }

    public void testLocalStatusOpenForEditExecutable() throws ScmException
    {
        core.runP4(null, P4_COMMAND, COMMAND_EDIT, "script1");

        WorkingCopyStatus status = wc.getLocalStatus();
        assertChange(status, "script1", FileStatus.State.MODIFIED, false, true);
    }

    public void testStatusOpenForEditAddExecutable() throws ScmException
    {
        core.runP4(null, P4_COMMAND, COMMAND_EDIT, FLAG_TYPE, "text+x", "file1");

        WorkingCopyStatus status = wc.getStatus();
        assertEquals(HEAD_REVISION, status.getRevision().getRevisionString());
        assertExecutable(status, "file1", true);
    }

    public void testLocalStatusOpenForEditAddExecutable() throws ScmException
    {
        core.runP4(null, P4_COMMAND, COMMAND_EDIT, FLAG_TYPE, "text+x", "file1");

        WorkingCopyStatus status = wc.getLocalStatus();
        assertExecutable(status, "file1", true);
    }

    public void testStatusOpenForEditRemoveExecutable() throws ScmException
    {
        core.runP4(null, P4_COMMAND, COMMAND_EDIT, FLAG_TYPE, "text", "script1");

        WorkingCopyStatus status = wc.getStatus();
        assertEquals(HEAD_REVISION, status.getRevision().getRevisionString());
        assertExecutable(status, "script1", false);
    }

    public void testLocalStatusOpenForEditRemoveExecutable() throws ScmException
    {
        core.runP4(null, P4_COMMAND, COMMAND_EDIT, FLAG_TYPE, "text", "script1");

        WorkingCopyStatus status = wc.getLocalStatus();
        assertExecutable(status, "script1", false);
    }

    public void testStatusOpenForEditBinary() throws ScmException
    {
        core.runP4(null, P4_COMMAND, COMMAND_EDIT, "bin1");

        WorkingCopyStatus status = wc.getStatus();
        assertEquals(HEAD_REVISION, status.getRevision().getRevisionString());
        assertChange(status, "bin1", FileStatus.State.MODIFIED, false, false);
    }

    public void testLocalStatusOpenForEditBinary() throws ScmException
    {
        core.runP4(null, P4_COMMAND, COMMAND_EDIT, "bin1");

        WorkingCopyStatus status = wc.getLocalStatus();
        assertChange(status, "bin1", FileStatus.State.MODIFIED, false, false);
    }

    public void testStatusOpenForEditAddBinary() throws ScmException
    {
        core.runP4(null, P4_COMMAND, COMMAND_EDIT, FLAG_TYPE, "binary", "file1");

        WorkingCopyStatus status = wc.getStatus();
        assertEquals(HEAD_REVISION, status.getRevision().getRevisionString());
        assertChange(status, "file1", FileStatus.State.MODIFIED, false, false);
    }

    public void testLocalStatusOpenForEditAddBinary() throws ScmException
    {
        core.runP4(null, P4_COMMAND, COMMAND_EDIT, FLAG_TYPE, "binary", "file1");

        WorkingCopyStatus status = wc.getLocalStatus();
        assertChange(status, "file1", FileStatus.State.MODIFIED, false, false);
    }

    public void testStatusOpenForEditRemoveBinary() throws ScmException
    {
        core.runP4(null, P4_COMMAND, COMMAND_EDIT, FLAG_TYPE, "text", "bin1");

        WorkingCopyStatus status = wc.getStatus();
        assertEquals(HEAD_REVISION, status.getRevision().getRevisionString());
        assertChange(status, "bin1", FileStatus.State.MODIFIED, false, true);
    }

    public void testLocalStatusOpenForEditRemoveBinary() throws ScmException
    {
        core.runP4(null, P4_COMMAND, COMMAND_EDIT, FLAG_TYPE, "text", "bin1");

        WorkingCopyStatus status = wc.getLocalStatus();
        assertChange(status, "bin1", FileStatus.State.MODIFIED, false, true);
    }

    public void testStatusOpenForAdd() throws ScmException, IOException
    {
        openForAdd(null);

        WorkingCopyStatus status = wc.getStatus();
        assertEquals(HEAD_REVISION, status.getRevision().getRevisionString());
        assertChange(status, "newfile", FileStatus.State.ADDED, false, true);
    }

    public void testLocalStatusOpenForAdd() throws ScmException, IOException
    {
        openForAdd(null);

        WorkingCopyStatus status = wc.getLocalStatus();
        assertChange(status, "newfile", FileStatus.State.ADDED, false, true);
    }

    public void testStatusOpenForAddBinary() throws ScmException, IOException
    {
        openForAdd("binary");

        WorkingCopyStatus status = wc.getStatus();
        assertEquals(HEAD_REVISION, status.getRevision().getRevisionString());
        assertChange(status, "newfile", FileStatus.State.ADDED, false, false);
    }

    public void testLocalStatusOpenForAddBinary() throws ScmException, IOException
    {
        openForAdd("binary");

        WorkingCopyStatus status = wc.getLocalStatus();
        assertChange(status, "newfile", FileStatus.State.ADDED, false, false);
    }

    public void testStatusOpenForAddExecutable() throws ScmException, IOException
    {
        openForAdd("text+x");
        
        WorkingCopyStatus status = wc.getStatus();
        assertEquals(HEAD_REVISION, status.getRevision().getRevisionString());
        assertExecutable(status, "newfile", true);
    }

    public void testLocalStatusOpenForAddExecutable() throws ScmException, IOException
    {
        openForAdd("text+x");

        WorkingCopyStatus status = wc.getLocalStatus();
        assertExecutable(status, "newfile", true);
    }

    public void testStatusOpenForDelete() throws ScmException, IOException
    {
        core.runP4(null, P4_COMMAND, COMMAND_DELETE, "dir1/file1");

        WorkingCopyStatus status = wc.getStatus();
        assertEquals(HEAD_REVISION, status.getRevision().getRevisionString());
        assertChange(status, "dir1/file1", FileStatus.State.DELETED, false, false);
    }

    public void testLocalStatusOpenForDelete() throws ScmException, IOException
    {
        core.runP4(null, P4_COMMAND, COMMAND_DELETE, "dir1/file1");

        WorkingCopyStatus status = wc.getLocalStatus();
        assertChange(status, "dir1/file1", FileStatus.State.DELETED, false, false);
    }

    public void testStatusOpenForIntegrate() throws ScmException, IOException
    {
        core.runP4(null, P4_COMMAND, COMMAND_INTEGRATE, "file1", "integrated");

        WorkingCopyStatus status = wc.getStatus();
        assertEquals(HEAD_REVISION, status.getRevision().getRevisionString());
        assertChange(status, "integrated", FileStatus.State.BRANCHED, false, true);
    }

    public void testLocalStatusOpenForIntegrate() throws ScmException, IOException
    {
        core.runP4(null, P4_COMMAND, COMMAND_INTEGRATE, "file1", "integrated");

        WorkingCopyStatus status = wc.getLocalStatus();
        assertChange(status, "integrated", FileStatus.State.BRANCHED, false, true);
    }

    public void testStatusOpenForIntegrateEdited() throws ScmException, IOException
    {
        openForMerge("file1");

        WorkingCopyStatus status = wc.getStatus();
        assertChange(status, "file1", FileStatus.State.MERGED, false, true);
    }

    public void testLocalStatusOpenForIntegrateEdited() throws ScmException, IOException
    {
        openForMerge("file1");

        WorkingCopyStatus status = wc.getLocalStatus();
        assertChange(status, "file1", FileStatus.State.MERGED, false, true);
    }

    public void testStatusOpenForIntegrateBinary() throws ScmException, IOException
    {
        openForMerge("bin1");
        
        WorkingCopyStatus status = wc.getStatus();
        assertChange(status, "bin1", FileStatus.State.MERGED, false, false);
    }

    public void testLocalStatusOpenForIntegrateBinary() throws ScmException, IOException
    {
        openForMerge("bin1");

        WorkingCopyStatus status = wc.getLocalStatus();
        assertChange(status, "bin1", FileStatus.State.MERGED, false, false);
    }

    public void testStatusOpenForIntegrateExecutable() throws ScmException, IOException
    {
        openForMerge("script1");

        WorkingCopyStatus status = wc.getStatus();
        assertChange(status, "script1", FileStatus.State.MERGED, false, true);
    }

    public void testLocalStatusOpenForIntegrateExecutable() throws ScmException, IOException
    {
        openForMerge("script1");
        
        WorkingCopyStatus status = wc.getLocalStatus();
        assertChange(status, "script1", FileStatus.State.MERGED, false, true);
    }

    public void testStatusOpenForIntegrateAddExecutable() throws ScmException, IOException
    {
        openForMerge("file1", "text+x");

        WorkingCopyStatus status = wc.getStatus();
        assertExecutable(status, "file1", true);
    }

    public void testLocalStatusOpenForIntegrateAddExecutable() throws ScmException, IOException
    {
        openForMerge("file1", "text+x");

        WorkingCopyStatus status = wc.getLocalStatus();
        assertExecutable(status, "file1", true);
    }

    public void testStatusOpenForIntegrateRemoveExecutable() throws ScmException, IOException
    {
        openForMerge("script1", "text");

        WorkingCopyStatus status = wc.getStatus();
        assertExecutable(status, "script1", false);
    }

    public void testLocalStatusOpenForIntegrateRemoveExecutable() throws ScmException, IOException
    {
        openForMerge("script1", "text");

        WorkingCopyStatus status = wc.getLocalStatus();
        assertExecutable(status, "script1", false);
    }

    public void testStatusOODEdited() throws ScmException, IOException
    {
        otherEdit();

        WorkingCopyStatus status = wc.getStatus();
        assertChange(status, "file1", FileStatus.State.UNCHANGED, true, false);
    }

    public void testLocalStatusIgnoresOOD() throws ScmException, IOException
    {
        otherEdit();

        WorkingCopyStatus status = wc.getLocalStatus();
        assertEquals(0, status.getChanges().size());
    }

    public void testStatusOODAdded() throws ScmException, IOException
    {
        File newFile = new File(otherClientRoot, "newfile");
        FileSystemUtils.createFile(newFile, "new");
        otherCore.runP4(null, P4_COMMAND, COMMAND_ADD, "newfile");
        otherCore.submit("comment");

        WorkingCopyStatus status = wc.getStatus();
        assertChange(status, "newfile", FileStatus.State.UNCHANGED, true, false);
    }

    public void testStatusOODDeleted() throws ScmException
    {
        otherCore.runP4(null, P4_COMMAND, COMMAND_DELETE, "file1");
        otherCore.submit("comment");

        WorkingCopyStatus status = wc.getStatus();
        assertChange(status, "file1", FileStatus.State.UNCHANGED, true, false);
    }

    public void testStatusOODAndLocalChange() throws ScmException
    {
        openForEdit("file1");

        otherCore.runP4(null, P4_COMMAND, COMMAND_DELETE, "file1");
        otherCore.submit("comment");

        WorkingCopyStatus status = wc.getStatus();
        assertChange(status, "file1", FileStatus.State.MODIFIED, true, true);
    }

    public void testLocalStatusOODAndLocalChange() throws ScmException
    {
        openForEdit("file1");

        otherCore.runP4(null, P4_COMMAND, COMMAND_DELETE, "file1");
        otherCore.submit("comment");

        WorkingCopyStatus status = wc.getLocalStatus();
        assertChange(status, "file1", FileStatus.State.MODIFIED, false, true);
    }

    public void testStatusEditEdited() throws ScmException, IOException
    {
        otherEdit();

        openForEdit("file1");
        core.runP4(null, P4_COMMAND, COMMAND_SYNC);

        WorkingCopyStatus status = wc.getStatus();
        assertChange(status, "file1", FileStatus.State.UNRESOLVED, false, false);
    }

    public void testStatusEditDeleted() throws ScmException
    {
        otherCore.runP4(null, P4_COMMAND, COMMAND_DELETE, "file1");
        otherCore.submit("comment");

        openForEdit("file1");
        core.runP4(null, P4_COMMAND, COMMAND_SYNC);

        WorkingCopyStatus status = wc.getStatus();
        assertChange(status, "file1", FileStatus.State.UNRESOLVED, false, false);
    }

    public void testStatusDeleteEdited() throws ScmException, IOException
    {
        otherEdit();

        core.runP4(null, P4_COMMAND, COMMAND_DELETE, "file1");
        core.runP4(null, P4_COMMAND, COMMAND_SYNC);

        WorkingCopyStatus status = wc.getStatus();
        assertChange(status, "file1", FileStatus.State.DELETED, false, false);
    }

    public void testStatusDeleteDeleted() throws ScmException
    {
        otherCore.runP4(null, P4_COMMAND, COMMAND_DELETE, "file1");
        otherCore.submit("comment");

        core.runP4(null, P4_COMMAND, COMMAND_DELETE, "file1");
        core.runP4(null, P4_COMMAND, COMMAND_SYNC);

        WorkingCopyStatus status = wc.getStatus();
        assertChange(status, "file1", FileStatus.State.DELETED, false, false);
    }

    public void testStatusUnresolvedMerge() throws ScmException
    {
        core.runP4(null, P4_COMMAND, COMMAND_EDIT, "branches/1/file1");
        core.submit("edit on branch");

        core.runP4(null, P4_COMMAND, COMMAND_INTEGRATE, "//depot/branches/1/...", "//depot/...");

        WorkingCopyStatus status = wc.getStatus();
        assertChange(status, "file1", FileStatus.State.UNRESOLVED, false, false);
    }

    public void testLocalStatusRestrictedToFiles() throws ScmException
    {
        openForEdit("bin1");
        openForEdit("file1");
        openForEdit("script1");

        WorkingCopyStatus status = wc.getLocalStatus("//depot/bin1", "//depot/script1");
        assertEquals(2, status.getChanges().size());
        assertEquals("bin1", status.getChanges().get(0).getPath());
        assertEquals("script1", status.getChanges().get(1).getPath());
    }

    public void testLocalStatusRestrictedToUnchangedFiles() throws ScmException
    {
        openForEdit("file1");

        WorkingCopyStatus status = wc.getLocalStatus("//depot/bin1", "//depot/script1");
        assertEquals(0, status.getChanges().size());
    }

    public void testLocalStatusDefaultChangelist() throws ScmException
    {
        openForEdit("bin1");
        openForEdit("file1");

        WorkingCopyStatus status = wc.getLocalStatus(":default");
        assertEquals(2, status.getChanges().size());
        assertEquals("bin1", status.getChanges().get(0).getPath());
        assertEquals("file1", status.getChanges().get(1).getPath());
    }

    public void testLocalStatusChangelist() throws ScmException
    {
        long changelist = core.createChangelist("test");
        openForEdit("bin1", changelist);
        openForEdit("file1");
        openForEdit("script1", changelist);

        WorkingCopyStatus status = wc.getLocalStatus(":" + Long.toString(changelist));
        assertEquals(2, status.getChanges().size());
        assertEquals("bin1", status.getChanges().get(0).getPath());
        assertEquals("script1", status.getChanges().get(1).getPath());
    }

    public void testLocalStatusChangelistNoChanges() throws ScmException
    {
        long changelist = core.createChangelist("test");
        openForEdit("file1");

        WorkingCopyStatus status = wc.getLocalStatus(":" + Long.toString(changelist));
        assertEquals(0, status.getChanges().size());
    }

    public void testUpdateAlreadyUpToDate() throws ScmException
    {
        wc.update();
        WorkingCopyStatus status = wc.getStatus();
        assertEquals(HEAD_REVISION, status.getRevision().getRevisionString());
        assertEquals(0, status.getChanges().size());
    }

    public void testSimpleUpdate() throws ScmException, IOException
    {
        otherEdit();

        wc.update();
        WorkingCopyStatus status = wc.getStatus();
        assertEquals("3", status.getRevision().getRevisionString());
        assertEquals(0, status.getChanges().size());
    }

    public void testUpdateMerge() throws ScmException, IOException
    {
        otherEdit();
        openForEdit("file1");

        wc.update();
        WorkingCopyStatus status = wc.getStatus();
        assertEquals("3", status.getRevision().getRevisionString());
        assertChange(status, "file1", FileStatus.State.MODIFIED, false, true);
    }

    public void testUpdateConflict() throws ScmException, IOException
    {
        otherEdit();

        openForEdit("file1");
        File file1 = new File(clientRoot, "file1");
        FileSystemUtils.createFile(file1, "this");

        wc.update();
        WorkingCopyStatus status = wc.getStatus();
        assertEquals("3", status.getRevision().getRevisionString());
        assertChange(status, "file1", FileStatus.State.UNRESOLVED, false, false);
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

    private void assertChange(WorkingCopyStatus status, String path, FileStatus.State state, boolean outOfDate, boolean text)
    {
        List<FileStatus> changes = status.getChanges();
        assertEquals(1, changes.size());
        FileStatus fs = changes.get(0);
        assertEquals(path, fs.getPath());
        assertEquals(state, fs.getState());
        assertFalse(fs.isDirectory());
        assertEquals(outOfDate, fs.isOutOfDate());
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
        assertEquals(message, warnings.remove(0));
    }

    public void setVerbosity(Verbosity verbosity)
    {
    }

    public void debug(String message)
    {
    }

    public void status(String message)
    {
        statuses.add(message);
    }

    public void warning(String message)
    {
        warnings.add(message);
    }

    public void error(String message)
    {
        errors.add(message);
    }

    public void error(String message, Throwable throwable)
    {
        errors.add(message);
    }

    public void enterContext()
    {
    }

    public void exitContext()
    {
    }

    public String inputPrompt(String question)
    {
        return null;
    }

    public String inputPrompt(String prompt, String defaultResponse)
    {
        return defaultResponse;
    }

    public String passwordPrompt(String question)
    {
        return null;
    }

    public Response ynPrompt(String question, Response defaultResponse)
    {
        return defaultResponse;
    }

    public Response ynaPrompt(String question, Response defaultResponse)
    {
        return defaultResponse;
    }
}
