package com.zutubi.pulse.core.personal;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.scm.RecordingScmFeedbackHandler;
import com.zutubi.pulse.core.scm.WorkingCopyContextImpl;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.core.util.PulseZipUtils;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
    private WorkingCopyContext context;
    private WorkingCopyStatusBuilder statusBuilder;

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

        context = new WorkingCopyContextImpl(wcDir, null, new TestPersonalBuildUI());
        statusBuilder = mock(WorkingCopyStatusBuilder.class);
        stub(statusBuilder.canDiff(eq(context), anyString())).toReturn(false);
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
        stub(statusBuilder.getLocalStatus(context)).toReturn(wcs);

        StandardPatchFileSupport.writePatchFile(statusBuilder, context, archiveFile);
        assertTrue(archiveFile.exists());
        PulseZipUtils.extractZip(archiveFile, targetDir);

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

        List<Feature> features = createAndApplyPatch(status);
        
        assertEquals(EDITED_CONTENT, IOUtils.fileToString(targetTestFile));
        assertEquals(0, features.size());
    }

    public void testApplyDelete() throws IOException, PulseException
    {
        assertTrue(wcTestFile.delete());

        WorkingCopyStatus status = new WorkingCopyStatus(wcDir);
        status.addFileStatus(new FileStatus(TEST_FILENAME, FileStatus.State.DELETED, false));

        List<Feature> features = createAndApplyPatch(status);

        assertFalse(targetTestFile.exists());
        assertEquals(0, features.size());
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

        List<Feature> features = createAndApplyPatch(status);

        assertFalse(nestedTargetDir.exists());
        assertFalse(nestedTargetFile.exists());
        assertEquals(0, features.size());
    }

    public void testModificationToNonExistantTarget() throws IOException, PulseException
    {
        final String MISSING_FILE_NAME = "notintarget";

        File modifiedWCFile = new File(wcDir, MISSING_FILE_NAME);
        FileSystemUtils.createFile(modifiedWCFile, "content");

        WorkingCopyStatus status = new WorkingCopyStatus(wcDir);
        status.addFileStatus(new FileStatus(MISSING_FILE_NAME, FileStatus.State.MODIFIED, false));

        List<Feature> features = createAndApplyPatch(status);

        assertEquals(1, features.size());
        assertThat(features.get(0).getSummary(), containsString("Target file 'notintarget' with status MODIFIED in patch does not exist"));
    }

    private List<Feature> createAndApplyPatch(WorkingCopyStatus status) throws PulseException, IOException
    {
        stub(statusBuilder.getLocalStatus(context)).toReturn(status);
        StandardPatchFileSupport.writePatchFile(statusBuilder, context, archiveFile);
        PatchArchive archive = new PatchArchive(archiveFile);
        return archive.apply(targetDir, EOLStyle.BINARY, new RecordingScmFeedbackHandler());
    }

}
