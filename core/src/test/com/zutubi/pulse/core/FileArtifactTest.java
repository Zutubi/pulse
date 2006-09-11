package com.zutubi.pulse.core;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.core.model.CommandResult;

import java.io.File;
import java.io.IOException;

/**
 * <class-comment/>
 */
public class FileArtifactTest extends PulseTestCase
{
    /**
     * The builds working directory, into which artifacts are created.
     */
    private File tmpSourceDir = null;

    /**
     * The storage directory, into which artifacts are copied for storage beyond the lifecycle of a builds working
     * directory.
     */
    private File tmpOutputDir = null;

    /**
     * The instance of the FileArtifact class being tested.
     */
    private FileArtifact fileArtifactObject;
    private CommandResult result;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmpSourceDir = FileSystemUtils.createTempDirectory();

        // setup the contents of the source directory - the output of a sample build.
        createBuildArtifact("file.txt");
        createBuildArtifact("file.tmp");
        createBuildArtifact("some/file-1.txt");
        createBuildArtifact("some/file-2.txt");
        createBuildArtifact("some/directory/file-xyz.txt");

        tmpOutputDir = FileSystemUtils.createTempDirectory();

        fileArtifactObject = new FileArtifact();
        fileArtifactObject.setName("test");
        fileArtifactObject.setFailIfNotPresent(false);
        result = new CommandResult("test");
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmpSourceDir);
        removeDirectory(tmpOutputDir);

        tmpSourceDir = null;
        tmpOutputDir = null;
        fileArtifactObject = null;
        result = null;

        super.tearDown();
    }

    private void createBuildArtifact(String relativePath) throws IOException
    {
        File f = new File(tmpSourceDir, relativePath);
        assertTrue(f.getParentFile().isDirectory() || f.getParentFile().mkdirs());
        assertTrue(f.createNewFile());
    }

    public void testCapturesExplicitFile()
    {
        fileArtifactObject.setFile("file.txt");
        fileArtifactObject.capture(result, tmpSourceDir, tmpOutputDir);

        assertArtifactCaptured("file.txt");
        assertCapturedArtifactCount(1);
    }

    public void testHandlesNonExistantFile()
    {
        fileArtifactObject.setFile("file.jpg");
        fileArtifactObject.capture(result, tmpSourceDir, tmpOutputDir);

        assertArtifactNotCaptured("file.jpg");
        assertCapturedArtifactCount(0);

        fileArtifactObject.setFailIfNotPresent(true);
        fileArtifactObject.setFile("file.jpg");
        try
        {
            fileArtifactObject.capture(result, tmpSourceDir, tmpOutputDir);
            fail();
        }
        catch (BuildException be)
        {
            // noop.
        }
        assertCapturedArtifactCount(0);
    }

    public void testSupportsWildCardMatchingOfSingleFile()
    {
        fileArtifactObject.setFile("*.tmp");
        fileArtifactObject.capture(result, tmpSourceDir, tmpOutputDir);

        assertArtifactCaptured("file.tmp");
        assertCapturedArtifactCount(1);
    }

    public void testSupportsWildCardMatchingOfMultipleFiles()
    {
        fileArtifactObject.setFile("some/file-*.txt");
        fileArtifactObject.capture(result, tmpSourceDir, tmpOutputDir);

        assertArtifactNotCaptured("file.txt");
        assertArtifactNotCaptured("file.tmp");
        assertArtifactCaptured("some/file-1.txt");
        assertArtifactCaptured("some/file-2.txt");
        assertArtifactNotCaptured("some/directory/file-xyz.txt");
        assertCapturedArtifactCount(1);
    }

    public void testSupportsWildCardMatchingAcrossMultipleDirectories()
    {
        fileArtifactObject.setFile("**/*.txt");
        fileArtifactObject.capture(result, tmpSourceDir, tmpOutputDir);

        assertArtifactCaptured("file.txt");
        assertArtifactNotCaptured("file.tmp");
        assertArtifactCaptured("some/file-1.txt");
        assertArtifactCaptured("some/file-2.txt");
        assertArtifactCaptured("some/directory/file-xyz.txt");
        assertCapturedArtifactCount(1);
    }

    public void testFileWithAbsolutePathIsCaptured()
    {
        File f = new File(tmpSourceDir, "file.txt");
        fileArtifactObject.setFile(f.getAbsolutePath());

        fileArtifactObject.capture(result, tmpSourceDir, tmpOutputDir);
        assertArtifactCaptured("file.txt");
        assertCapturedArtifactCount(1);
    }

    public void testFileWithAbsolutePathAndWildcardsIsCaptured()
    {
        File f = new File(tmpSourceDir, "file.*");
        fileArtifactObject.setFile(f.getAbsolutePath());

        fileArtifactObject.capture(result, tmpSourceDir, tmpOutputDir);
        assertArtifactCaptured("file.txt");
        assertArtifactCaptured("file.tmp");
        assertCapturedArtifactCount(1);
    }

    public void testAbsoluteFileWithWildcardAtFileBoundryIsCaptured()
    {
        File f = new File(tmpSourceDir, "some/directory/*.txt");
        fileArtifactObject.setFile(f.getAbsolutePath());
        fileArtifactObject.capture(result, tmpSourceDir, tmpOutputDir);

        assertArtifactCaptured("file-xyz.txt");
        assertCapturedArtifactCount(1);
    }

    private void assertArtifactCaptured(String relativePath)
    {
        // need to add 'test/' since this is the artifact name prefix.
        File artifactFile = new File(tmpOutputDir, "test/" + relativePath);
        assertTrue(artifactFile.isFile());
    }

    private void assertArtifactNotCaptured(String relativePath)
    {
        // need to add 'test/' since this is the artifact name prefix.
        File artifactFile = new File(tmpOutputDir, "test/" + relativePath);
        assertFalse(artifactFile.isFile());
    }

    private void assertCapturedArtifactCount(int count)
    {
        assertEquals(count, result.getArtifacts().size());
    }

}
