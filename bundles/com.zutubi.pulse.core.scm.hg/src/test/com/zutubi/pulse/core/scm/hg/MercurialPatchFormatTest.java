package com.zutubi.pulse.core.scm.hg;

import com.zutubi.pulse.core.scm.WorkingCopyContextImpl;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.api.WorkingCopyContext;
import com.zutubi.pulse.core.scm.patch.api.FileStatus;
import com.zutubi.pulse.core.ui.TestUI;
import com.zutubi.util.config.CompositeConfig;
import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;

public class MercurialPatchFormatTest extends MercurialTestBase
{
    public static final String FILENAME_FILE1 = "file1.txt";
    public static final String FILENAME_FILE2 = "file2.txt";
    
    private File workingDir;
    private File patchFile;
    private File file1;
    private File file2;
    private MercurialCore workingCore;
    private MercurialCore buildCore;
    private MercurialWorkingCopy workingCopy;
    private WorkingCopyContext devContext;
    private MercurialPatchFormat patchFormat;
    private TestUI ui;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        workingDir = new File(tmp, "working");
        patchFile = new File(workingDir, "patch");
        file1 = new File(workingDir, FILENAME_FILE1);
        file2 = new File(workingDir, FILENAME_FILE2);
        
        workingCore = new MercurialCore();
        workingCore.setWorkingDirectory(workingDir.getParentFile());
        workingCore.clone(null, repository, null, null, workingDir.getName());
        workingCore.setWorkingDirectory(workingDir);
        workingCore.update(null, null);

        buildCore = new MercurialCore();
        buildCore.setWorkingDirectory(baseDir.getParentFile());
        buildCore.clone(null, repository, null, null, baseDir.getName());
        buildCore.setWorkingDirectory(baseDir);
        buildCore.update(null, null);
        
        workingCopy = new MercurialWorkingCopy();
        ui = new TestUI();
        devContext = new WorkingCopyContextImpl(workingDir, new CompositeConfig(), ui);
        patchFormat = new MercurialPatchFormat();
    }
    
    public void testWritePatchFileNoChanges() throws ScmException
    {
        assertFalse(patchFormat.writePatchFile(workingCopy, devContext, patchFile));
        assertFalse(patchFile.exists());
        assertThat(ui.getStatusMessages(), hasItem(containsString("created an empty patch file")));
    }

    public void testSimpleChange() throws ScmException, IOException
    {
        final String NEW_CONTENT = "this is\nnew content";
        
        FileSystemUtils.createFile(file1, NEW_CONTENT);
        assertTrue(patchFormat.writePatchFile(workingCopy, devContext, patchFile));
        assertTrue(patchFile.exists());

        List<FileStatus> fileStatuses = patchFormat.readFileStatuses(patchFile);
        assertEquals(1, fileStatuses.size());
        assertModifiedStatus(fileStatuses.get(0), file1.getName());

        patchFormat.applyPatch(buildContext, patchFile, baseDir, null, null);
        assertEquals(NEW_CONTENT, IOUtils.fileToString(new File(baseDir, file1.getName())));
    }

    public void testSimpleAdd() throws ScmException, IOException
    {
        final String CONTENT = "this is\na new file";
        final String FILENAME = "new";
        
        FileSystemUtils.createFile(new File(workingDir, FILENAME), CONTENT);
        workingCore.add(null, FILENAME);
        assertTrue(patchFormat.writePatchFile(workingCopy, devContext, patchFile));
        assertTrue(patchFile.exists());

        List<FileStatus> fileStatuses = patchFormat.readFileStatuses(patchFile);
        assertEquals(1, fileStatuses.size());
        assertAddedStatus(fileStatuses.get(0), FILENAME);

        patchFormat.applyPatch(buildContext, patchFile, baseDir, null, null);
        assertEquals(CONTENT, IOUtils.fileToString(new File(baseDir, FILENAME)));
    }

    public void testSimpleDelete() throws ScmException, IOException
    {
        File file1 = new File(workingDir, FILENAME_FILE1);
        assertTrue(file1.delete());
        workingCore.remove(null, FILENAME_FILE1);
        assertTrue(patchFormat.writePatchFile(workingCopy, devContext, patchFile));
        assertTrue(patchFile.exists());

        List<FileStatus> fileStatuses = patchFormat.readFileStatuses(patchFile);
        assertEquals(1, fileStatuses.size());
        assertRemovedStatus(fileStatuses.get(0), FILENAME_FILE1);

        File deletedDest = new File(baseDir, FILENAME_FILE1);
        assertTrue(deletedDest.isFile());
        patchFormat.applyPatch(buildContext, patchFile, baseDir, null, null);
        assertFalse(deletedDest.exists());
    }
    
    public void testSingleRevision() throws ScmException, IOException
    {
        assertTrue(patchFormat.writePatchFile(workingCopy, devContext, patchFile, ":" + REVISION_DEFAULT_PREVIOUS));
        assertTrue(patchFile.exists());

        List<FileStatus> fileStatuses = patchFormat.readFileStatuses(patchFile);
        assertEquals(1, fileStatuses.size());
        assertModifiedStatus(fileStatuses.get(0), CONTENT_FILE_PATH);
        
        buildCore.update(null, REVISION_DEFAULT_TWO_PREVIOUS);
        assertContent(CONTENT_DEFAULT_TWO_PREVIOUS);
        patchFormat.applyPatch(buildContext, patchFile, baseDir, null, null);
        assertContent(CONTENT_DEFAULT_PREVIOUS);
    }

    public void testRevisionRange() throws ScmException, IOException
    {
        assertTrue(patchFormat.writePatchFile(workingCopy, devContext, patchFile, ":" + REVISION_DEFAULT_TWO_PREVIOUS + ":" + REVISION_DEFAULT_LATEST));
        assertTrue(patchFile.exists());

        List<FileStatus> fileStatuses = patchFormat.readFileStatuses(patchFile);
        assertEquals(1, fileStatuses.size());
        assertModifiedStatus(fileStatuses.get(0), CONTENT_FILE_PATH);
        
        buildCore.update(null, REVISION_DEFAULT_TWO_PREVIOUS);
        assertContent(CONTENT_DEFAULT_TWO_PREVIOUS);
        patchFormat.applyPatch(buildContext, patchFile, baseDir, null, null);
        assertContent(CONTENT_DEFAULT_LATEST);
    }

    public void testSpecificFiles() throws IOException, ScmException
    {
        final String NEW_CONTENT = "this is\nnew content";
        
        FileSystemUtils.createFile(file1, NEW_CONTENT);
        FileSystemUtils.createFile(file2, NEW_CONTENT);
        assertTrue(patchFormat.writePatchFile(workingCopy, devContext, patchFile, FILENAME_FILE1));
        assertTrue(patchFile.exists());

        List<FileStatus> fileStatuses = patchFormat.readFileStatuses(patchFile);
        assertEquals(1, fileStatuses.size());
        assertModifiedStatus(fileStatuses.get(0), file1.getName());

        String original2Content = IOUtils.fileToString(new File(baseDir, file2.getName()));
        patchFormat.applyPatch(buildContext, patchFile, baseDir, null, null);
        assertEquals(NEW_CONTENT, IOUtils.fileToString(new File(baseDir, file1.getName())));
        assertEquals(original2Content, IOUtils.fileToString(new File(baseDir, file2.getName())));
    }
    
    private void assertContent(String expected) throws IOException
    {
        assertEquals(expected, IOUtils.fileToString(new File(baseDir, CONTENT_FILE_PATH)));
    }

    private void assertModifiedStatus(FileStatus fileStatus, String path)
    {
        assertEquals(path, fileStatus.getPath());
        assertEquals(FileStatus.PayloadType.DIFF, fileStatus.getPayloadType());
        assertEquals(FileStatus.State.MODIFIED, fileStatus.getState());
    }

    private void assertAddedStatus(FileStatus fileStatus, String path)
    {
        assertEquals(path, fileStatus.getPath());
        assertEquals(FileStatus.PayloadType.FULL, fileStatus.getPayloadType());
        assertEquals(FileStatus.State.ADDED, fileStatus.getState());
    }

    private void assertRemovedStatus(FileStatus fileStatus, String path)
    {
        assertEquals(path, fileStatus.getPath());
        assertEquals(FileStatus.PayloadType.NONE, fileStatus.getPayloadType());
        assertEquals(FileStatus.State.DELETED, fileStatus.getState());
    }
}
