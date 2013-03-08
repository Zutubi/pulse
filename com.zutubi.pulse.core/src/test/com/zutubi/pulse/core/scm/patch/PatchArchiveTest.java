package com.zutubi.pulse.core.scm.patch;

import com.google.common.io.Files;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.scm.RecordingScmFeedbackHandler;
import com.zutubi.pulse.core.scm.WorkingCopyContextImpl;
import com.zutubi.pulse.core.scm.api.EOLStyle;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.WorkingCopyContext;
import com.zutubi.pulse.core.scm.patch.api.FileStatus;
import com.zutubi.pulse.core.scm.patch.api.WorkingCopyStatus;
import com.zutubi.pulse.core.scm.patch.api.WorkingCopyStatusBuilder;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.core.ui.TestUI;
import com.zutubi.pulse.core.util.PulseZipUtils;
import com.zutubi.util.io.FileSystemUtils;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;

public class PatchArchiveTest extends PulseTestCase
{
    private static final String TEST_FILENAME = "test.txt";
    private static final String TEST_FILE_CONTENT = "test file";
    
    private static final String TEST_DIFF = "--- test.txt\t2010-08-30 15:06:16.840449310 +1000\n" +
            "+++ test.txt\t2010-08-30 15:06:24.910449120 +1000\n" +
            "@@ -1 +1 @@\n" +
            "-test file\n" +
            "+edited content\n" +
            "\\ No newline at end of file";

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

        context = new WorkingCopyContextImpl(wcDir, null, new TestUI());
        statusBuilder = mock(WorkingCopyStatusBuilder.class);
        stub(statusBuilder.canDiff(eq(context), anyString())).toReturn(false);
    }


    protected void tearDown() throws Exception
    {
        removeDirectory(tempDir);
        super.tearDown();
    }

    public void testPropertySerialisation() throws PulseException, IOException
    {
        WorkingCopyStatus wcs = new WorkingCopyStatus(wcDir);
        FileStatus fs = new FileStatus(TEST_FILENAME, FileStatus.State.UNCHANGED, false);
        fs.setProperty(FileStatus.PROPERTY_EOL_STYLE, EOLStyle.CARRIAGE_RETURN.toString());
        wcs.addFileStatus(fs);
        stub(statusBuilder.getLocalStatus(context)).toReturn(wcs);

        StandardPatchFormat patchFormat = new StandardPatchFormat();
        patchFormat.writePatchFile(statusBuilder, context, archiveFile);
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

        assertEquals(EDITED_CONTENT, Files.toString(targetTestFile, Charset.defaultCharset()));
        assertEquals(0, features.size());
    }

    public void testModificationToFileWithExecutablePermissions() throws IOException, PulseException
    {
        final String EDITED_CONTENT = "edited content";
        
        FileSystemUtils.setExecutable(targetTestFile);
        FileSystemUtils.createFile(wcTestFile, EDITED_CONTENT);

        WorkingCopyStatus status = new WorkingCopyStatus(wcDir);
        status.addFileStatus(new FileStatus(TEST_FILENAME, FileStatus.State.MODIFIED, false));

        List<Feature> features = createAndApplyPatch(status);

        assertEquals(EDITED_CONTENT, Files.toString(targetTestFile, Charset.defaultCharset()));
        assertEquals(FileSystemUtils.PERMISSION_OWNER_EXECUTE, FileSystemUtils.getPermissions(targetTestFile) & FileSystemUtils.PERMISSION_OWNER_EXECUTE);
        assertEquals(0, features.size());
    }

    public void testApplyDiffToFileWithExecutablePermissions() throws IOException, PulseException
    {
        final String EDITED_CONTENT = "edited content";

        stub(statusBuilder.canDiff(eq(context), anyString())).toReturn(true);
        doAnswer(new Answer<Object>()
        {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                OutputStream os = (OutputStream) invocationOnMock.getArguments()[2];
                OutputStreamWriter writer = new OutputStreamWriter(os);
                writer.write(TEST_DIFF);
                writer.flush();
                return null;
            }
        }).when(statusBuilder).diff(eq(context), anyString(), Matchers.<OutputStream>anyObject());
        
        FileSystemUtils.setExecutable(targetTestFile);
        FileSystemUtils.createFile(wcTestFile, EDITED_CONTENT);

        WorkingCopyStatus status = new WorkingCopyStatus(wcDir);
        status.addFileStatus(new FileStatus(TEST_FILENAME, FileStatus.State.MODIFIED, false));

        List<Feature> features = createAndApplyPatch(status);

        assertEquals(EDITED_CONTENT, Files.toString(targetTestFile, Charset.defaultCharset()));
        assertEquals(FileSystemUtils.PERMISSION_OWNER_EXECUTE, FileSystemUtils.getPermissions(targetTestFile) & FileSystemUtils.PERMISSION_OWNER_EXECUTE);
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
    
    public void testApplyNewEmptyDirectory() throws PulseException, IOException
    {
        final String DIR_NAME = "iamnew";

        File newDir = new File(wcDir, DIR_NAME);
        assertTrue(newDir.mkdir());

        WorkingCopyStatus status = new WorkingCopyStatus(wcDir);
        status.addFileStatus(new FileStatus(DIR_NAME, FileStatus.State.ADDED, true));

        createAndApplyPatch(status);

        assertTrue("Expected directory to be created", new File(targetDir, DIR_NAME).isDirectory());
    }

    private List<Feature> createAndApplyPatch(WorkingCopyStatus status) throws PulseException, IOException
    {
        stub(statusBuilder.getLocalStatus(context)).toReturn(status);
        StandardPatchFormat patchFormat = new StandardPatchFormat();
        patchFormat.writePatchFile(statusBuilder, context, archiveFile);
        PatchArchive archive = new PatchArchive(archiveFile);
        return archive.apply(targetDir, new PulseExecutionContext(), mock(ScmClient.class), new RecordingScmFeedbackHandler());
    }
}
