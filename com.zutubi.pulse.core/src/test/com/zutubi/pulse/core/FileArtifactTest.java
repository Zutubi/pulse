package com.zutubi.pulse.core;

import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;

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
    private File fileTxt;
    private File someFile;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmpSourceDir = FileSystemUtils.createTempDir();

        // setup the contents of the source directory - the output of a sample build.
        fileTxt = createBuildArtifact("file.txt");
        createBuildArtifact("file.tmp");
        createBuildArtifact("some/file-1.txt");
        someFile = createBuildArtifact("some/file-2.txt");
        createBuildArtifact("some/directory/file-xyz.txt");

        tmpOutputDir = FileSystemUtils.createTempDir();

        fileArtifactObject = new FileArtifact();
        fileArtifactObject.setName("test");
        fileArtifactObject.setFailIfNotPresent(false);
        result = new CommandResult("test");
        result.commence();
        result.complete();
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmpSourceDir);
        removeDirectory(tmpOutputDir);

        someFile = null;
        fileTxt = null;
        tmpSourceDir = null;
        tmpOutputDir = null;
        fileArtifactObject = null;
        result = null;

        super.tearDown();
    }

    private File createBuildArtifact(String relativePath) throws IOException
    {
        File f = new File(tmpSourceDir, relativePath);
        assertTrue(f.getParentFile().isDirectory() || f.getParentFile().mkdirs());
        assertTrue(f.createNewFile());
        return f;
    }

    public void testCapturesExplicitFile()
    {
        fileArtifactObject.setFile("file.txt");
        capture();

        assertArtifactCaptured("file.txt");
        assertCapturedArtifactCount(1);
    }

    public void testHandlesNonExistantFile()
    {
        fileArtifactObject.setFile("file.jpg");
        capture();

        assertArtifactNotCaptured("file.jpg");
        assertCapturedArtifactCount(0);

        fileArtifactObject.setFailIfNotPresent(true);
        fileArtifactObject.setFile("file.jpg");
        capture();
        assertCapturedArtifactCount(0);
        assertTrue(result.errored());
    }

    public void testSupportsWildCardMatchingOfSingleFile()
    {
        fileArtifactObject.setFile("*.tmp");
        capture();

        assertArtifactCaptured("file.tmp");
        assertCapturedArtifactCount(1);
    }

    public void testSupportsWildCardMatchingOfMultipleFiles()
    {
        fileArtifactObject.setFile("some/file-*.txt");
        capture();

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
        capture();

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

        capture();
        assertArtifactCaptured("file.txt");
        assertCapturedArtifactCount(1);
    }

    public void testFileWithAbsolutePathAndWildcardsIsCaptured()
    {
        File f = new File(tmpSourceDir, "file.*");
        fileArtifactObject.setFile(f.getAbsolutePath());

        capture();
        assertArtifactCaptured("file.txt");
        assertArtifactCaptured("file.tmp");
        assertCapturedArtifactCount(1);
    }

    public void testAbsoluteFileWithWildcardAtFileBoundryIsCaptured()
    {
        File f = new File(tmpSourceDir, "some/directory/*.txt");
        fileArtifactObject.setFile(f.getAbsolutePath());
        capture();

        assertArtifactCaptured("file-xyz.txt");
        assertCapturedArtifactCount(1);
    }

    public void testIgnoreStaleNotStale()
    {
        fileArtifactObject.setIgnoreStale(true);
        fileArtifactObject.setFile("file.txt");
        capture(0);

        assertArtifactCaptured("file.txt");
        assertCapturedArtifactCount(1);
    }

    public void testIgnoreStaleIsStale()
    {
        fileArtifactObject.setIgnoreStale(true);
        fileArtifactObject.setFile("file.txt");
        capture(fileTxt.lastModified()  + 1);

        assertCapturedArtifactCount(0);
    }

    public void testIgnoreStaleMultipleStaleFiles()
    {
        fileArtifactObject.setIgnoreStale(true);
        fileArtifactObject.setFile("some/file-*.txt");
        capture(someFile.lastModified() + 1);

        assertCapturedArtifactCount(0);
    }

    private void capture()
    {
        capture(-1);
    }

    private void capture(long recipeStartTime)
    {
        RecipePaths paths = new SimpleRecipePaths(tmpSourceDir, null);
        PulseExecutionContext context = new PulseExecutionContext();
        context.setWorkingDir(paths.getBaseDir());
        context.addValue(NAMESPACE_INTERNAL, PROPERTY_RECIPE_PATHS, paths);
        if(recipeStartTime > 0)
        {
            context.addString(NAMESPACE_INTERNAL, PROPERTY_RECIPE_TIMESTAMP_MILLIS, Long.toString(recipeStartTime));
        }

        context.addString(NAMESPACE_INTERNAL, PROPERTY_OUTPUT_DIR, tmpOutputDir.getAbsolutePath());

        fileArtifactObject.capture(result, context);
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
