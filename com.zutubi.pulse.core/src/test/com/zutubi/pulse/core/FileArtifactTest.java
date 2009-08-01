package com.zutubi.pulse.core;

import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FileArtifactTest extends PulseTestCase
{
    // Need to add 'test/' to captured paths since this is the artifact name prefix.
    private static final String CAPTURED_PREFIX = "test/";

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
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmpSourceDir);
        removeDirectory(tmpOutputDir);
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

        assertSingleFileCaptured("file.txt");
        assertCapturedArtifactCount(1);
    }

    public void testHandlesNonExistantFile()
    {
        fileArtifactObject.setFile("file.jpg");

        capture();

        assertFilesNotCaptured("file.jpg");
        assertCapturedArtifactCount(0);
        assertFalse(result.errored());
    }

    public void testFailIfNotPresent()
    {
        fileArtifactObject.setFailIfNotPresent(true);
        fileArtifactObject.setFile("file.jpg");

        capture();

        assertCapturedArtifactCount(0);
        assertTrue(result.errored());
    }

    public void testFailIfNotPresentWithWildcards()
    {
        fileArtifactObject.setFailIfNotPresent(true);
        fileArtifactObject.setFile("**/*.jpg");

        capture();

        assertCapturedArtifactCount(0);
        assertTrue(result.errored());
    }

    public void testFailIfNotPresentWithWildcardsAbsolute()
    {
         File f = new File(tmpSourceDir, "bile.*");
        fileArtifactObject.setFailIfNotPresent(true);
        fileArtifactObject.setFile(f.getAbsolutePath());

        capture();

        assertCapturedArtifactCount(0);
        assertTrue(result.errored());
    }
    
    public void testSupportsWildCardMatchingOfSingleFile()
    {
        fileArtifactObject.setFile("*.tmp");

        capture();

        assertSingleFileCaptured("file.tmp");
        assertCapturedArtifactCount(1);
    }

    public void testSupportsWildCardMatchingOfSingleNestedFile()
    {
        fileArtifactObject.setFile("**/file-xyz.txt");

        capture();

        assertSingleFileCaptured("file-xyz.txt");
        assertCapturedArtifactCount(1);
    }

    public void testSupportsWildCardMatchingOfMultipleFiles()
    {
        fileArtifactObject.setFile("some/file-*.txt");

        capture();

        assertMultipleFilesCaptured("some/file-1.txt", "some/file-2.txt");
        assertFilesNotCaptured("file.txt", "file.tmp", "some/directory/file-xyz.txt");
        assertCapturedArtifactCount(1);
    }

    public void testSupportsWildCardMatchingAcrossMultipleDirectories()
    {
        fileArtifactObject.setFile("**/*.txt");

        capture();

        assertMultipleFilesCaptured("file.txt", "some/file-1.txt", "some/file-2.txt", "some/directory/file-xyz.txt");
        assertFilesNotCaptured("file.tmp");
        assertCapturedArtifactCount(1);
    }

    public void testFileWithAbsolutePathIsCaptured()
    {
        fileArtifactObject.setFile(fileTxt.getAbsolutePath());

        capture();

        assertSingleFileCaptured("file.txt");
        assertCapturedArtifactCount(1);
    }

    public void testFileWithAbsolutePathAndWildcardsIsCaptured()
    {
        File f = new File(tmpSourceDir, "file.*");
        fileArtifactObject.setFile(f.getAbsolutePath());

        capture();

        assertMultipleFilesCaptured("file.txt", "file.tmp");
        assertCapturedArtifactCount(1);
    }

    public void testAbsoluteFileWithWildcardAtFileBoundryIsCaptured()
    {
        File f = new File(tmpSourceDir, "some/directory/*.txt");
        fileArtifactObject.setFile(f.getAbsolutePath());

        capture();

        assertSingleFileCaptured("file-xyz.txt");
        assertCapturedArtifactCount(1);
    }

    public void testFileWithForwardSlashOnWindows()
    {
        if (SystemUtils.IS_WINDOWS)
        {
            fileArtifactObject.setFile("/file.txt");

            capture();

            assertFilesNotCaptured("file.txt");
            assertCapturedArtifactCount(0);
        }
    }

    public void testAbsoluteFileWithForwardSlashOnWindows()
    {
        if (SystemUtils.IS_WINDOWS)
        {
            File f = new File(tmpSourceDir, "file.txt");
            fileArtifactObject.setFile(f.getAbsolutePath().substring(2).replace('\\', '/'));

            capture();

            assertSingleFileCaptured("file.txt");
            assertCapturedArtifactCount(1);
        }
    }

    public void testFileStartingWithDot()
    {
        fileArtifactObject.setFile("./file.txt");

        capture();

        assertSingleFileCaptured("file.txt");
        assertCapturedArtifactCount(1);
    }

    public void testFileContainingDot()
    {
        fileArtifactObject.setFile("some/./file-1.txt");

        capture();

        assertSingleFileCaptured("file-1.txt");
        assertCapturedArtifactCount(1);
    }

    public void testFileContainingDotDot()
    {
        fileArtifactObject.setFile("some/../file.txt");

        capture();

        assertSingleFileCaptured("file.txt");
        assertCapturedArtifactCount(1);
    }
    
    public void testIgnoreStaleNotStale()
    {
        fileArtifactObject.setIgnoreStale(true);
        fileArtifactObject.setFile("file.txt");

        capture(0);

        assertSingleFileCaptured("file.txt");
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

    private void assertSingleFileCaptured(String relativePath)
    {
        // Check the file has been copied
        File artifactFile = new File(tmpOutputDir, CAPTURED_PREFIX + relativePath);
        assertTrue(artifactFile.isFile());

        // Check the artifact has been registered under its file name
        List<StoredArtifact> artifacts = result.getArtifacts();
        assertEquals(1, artifacts.size());
        StoredArtifact storedArtifact = artifacts.get(0);
        assertEquals(1, storedArtifact.getChildren().size());
        assertNotNull(storedArtifact.findFile(CAPTURED_PREFIX + artifactFile.getName()));
    }

    private void assertMultipleFilesCaptured(String... relativePaths)
    {
        List<StoredArtifact> artifacts = result.getArtifacts();
        assertEquals(1, artifacts.size());
        StoredArtifact storedArtifact = artifacts.get(0);
        assertEquals(relativePaths.length, storedArtifact.getChildren().size());

        for (String relativePath: relativePaths)
        {
            // Check the file has been copied
            File artifactFile = new File(tmpOutputDir, CAPTURED_PREFIX + relativePath);
            assertTrue(artifactFile.isFile());

            // Check the artifact has been registered under its relative path
            assertNotNull(storedArtifact.findFile(CAPTURED_PREFIX + relativePath));
        }
    }

    private void assertFilesNotCaptured(String... relativePaths)
    {
        for (String relativePath: relativePaths)
        {
            File artifactFile = new File(tmpOutputDir, CAPTURED_PREFIX + relativePath);
            assertFalse(artifactFile.isFile());
        }
    }

    private void assertCapturedArtifactCount(int count)
    {
        assertEquals(count, result.getArtifacts().size());
    }
}
