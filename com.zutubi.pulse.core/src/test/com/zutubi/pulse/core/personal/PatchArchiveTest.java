package com.zutubi.pulse.core.personal;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.scm.api.EOLStyle;
import com.zutubi.pulse.core.scm.api.FileStatus;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.WorkingCopyStatus;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.core.util.ZipUtils;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.File;
import java.io.IOException;

public class PatchArchiveTest extends PulseTestCase
{
    private static final String TEST_FILENAME = "test.txt";
    private static final String TEST_FILE_CONTENT = "test file";

    private File tempDir;
    private File wcDir;
    private File targetDir;
    private File archiveFile;
    private File wcTestFile;
    private File targetTestFile;

    protected void setUp() throws Exception
    {
        super.setUp();
        tempDir = FileSystemUtils.createTempDir(PatchArchiveTest.class.getName(), "");

        wcDir = new File(tempDir, "wc");
        assertTrue(wcDir.mkdir());
        targetDir = new File(tempDir, "target");
        assertTrue(targetDir.mkdir());
        archiveFile = new File(tempDir, "patch.zip");

        wcTestFile = new File(wcDir, TEST_FILENAME);
        FileSystemUtils.createFile(wcTestFile, TEST_FILE_CONTENT);

        targetTestFile = new File(targetDir, TEST_FILENAME);
        FileSystemUtils.createFile(targetTestFile, TEST_FILE_CONTENT);
    }


    protected void tearDown() throws Exception
    {
        FileSystemUtils.rmdir(tempDir);
        super.tearDown();
    }

    public void testPropertySerialisation() throws PulseException, IOException
    {
        WorkingCopyStatus wcs = new WorkingCopyStatus(wcDir);
        FileStatus fs = new FileStatus(TEST_FILENAME, FileStatus.State.UNCHANGED, false);
        fs.setProperty(FileStatus.PROPERTY_EOL_STYLE, EOLStyle.CARRIAGE_RETURN.toString());
        wcs.addFileStatus(fs);

        new PatchArchive(new Revision("1"), wcs, archiveFile, null);
        assertTrue(archiveFile.exists());
        ZipUtils.extractZip(archiveFile, targetDir);

        PatchArchive archive = new PatchArchive(archiveFile);
        fs = archive.getMetadata().getFileStatus(TEST_FILENAME);
        assertEquals(EOLStyle.CARRIAGE_RETURN.toString(), fs.getProperty(FileStatus.PROPERTY_EOL_STYLE));
    }

    public void testApplyModification() throws IOException, PulseException
    {
        String EDITED_CONTENT = "edited content";

        FileSystemUtils.createFile(wcTestFile, EDITED_CONTENT);

        WorkingCopyStatus status = new WorkingCopyStatus(wcDir);
        status.addFileStatus(new FileStatus(TEST_FILENAME, FileStatus.State.MODIFIED, false));

        CommandResult commandResult = createAndApplyPatch(status);
        
        assertEquals(EDITED_CONTENT, IOUtils.fileToString(targetTestFile));
        assertEquals(0, commandResult.getFeatures().size());
    }

    public void testApplyDelete() throws IOException, PulseException
    {
        assertTrue(wcTestFile.delete());

        WorkingCopyStatus status = new WorkingCopyStatus(wcDir);
        status.addFileStatus(new FileStatus(TEST_FILENAME, FileStatus.State.DELETED, false));

        CommandResult commandResult = createAndApplyPatch(status);

        assertFalse(targetTestFile.exists());
        assertEquals(0, commandResult.getFeatures().size());
    }

    public void testApplyNestedDeletes() throws IOException, PulseException
    {
        File nestedTargetDir = new File(targetDir, "nested");
        assertTrue(nestedTargetDir.mkdir());
        File nestedTargetFile = new File(nestedTargetDir, "f");
        FileSystemUtils.createFile(nestedTargetFile, "contents");

        WorkingCopyStatus status = new WorkingCopyStatus(wcDir);
        status.addFileStatus(new FileStatus("nested", FileStatus.State.DELETED, false));
        status.addFileStatus(new FileStatus("nested/f", FileStatus.State.DELETED, false));

        CommandResult commandResult = createAndApplyPatch(status);

        assertFalse(nestedTargetDir.exists());
        assertFalse(nestedTargetFile.exists());
        assertEquals(0, commandResult.getFeatures().size());
    }

    public void testModificationToNonExistantTarget() throws IOException, PulseException
    {
        final String MISSING_FILE_NAME = "notintarget";

        File modifiedWCFile = new File(wcDir, MISSING_FILE_NAME);
        FileSystemUtils.createFile(modifiedWCFile, "content");

        WorkingCopyStatus status = new WorkingCopyStatus(wcDir);
        status.addFileStatus(new FileStatus(MISSING_FILE_NAME, FileStatus.State.MODIFIED, false));

        CommandResult commandResult = createAndApplyPatch(status);

        assertEquals(1, commandResult.getFeatures().size());
        assertThat(commandResult.getFeatures().get(0).getSummary(), containsString("Target file 'notintarget' with status MODIFIED in patch does not exist"));
    }

    public void testApplyNewEmptyDirectory() throws PulseException
    {
        final String DIR_NAME = "iamnew";

        File newDir = new File(wcDir, DIR_NAME);
        assertTrue(newDir.mkdir());

        WorkingCopyStatus status = new WorkingCopyStatus(newDir);
        status.addFileStatus(new FileStatus(DIR_NAME, FileStatus.State.ADDED, true));

        createAndApplyPatch(status);
        
        assertTrue("Expected directory to be created", new File(targetDir, DIR_NAME).isDirectory());
    }

    private CommandResult createAndApplyPatch(WorkingCopyStatus status) throws PulseException
    {
        PatchArchive archive = new PatchArchive(new Revision("1"), status, archiveFile, null);
        CommandResult commandResult = new CommandResult("test");
        archive.apply(targetDir, EOLStyle.BINARY, commandResult);
        return commandResult;
    }

}
