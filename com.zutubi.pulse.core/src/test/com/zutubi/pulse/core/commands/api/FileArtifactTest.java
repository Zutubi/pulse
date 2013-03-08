package com.zutubi.pulse.core.commands.api;

import com.google.common.io.Files;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.engine.api.BuildProperties;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.io.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import static com.zutubi.pulse.core.engine.api.BuildProperties.NAMESPACE_INTERNAL;
import static com.zutubi.pulse.core.engine.api.BuildProperties.PROPERTY_OUTPUT_DIR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class FileArtifactTest extends PulseTestCase
{
    private static final String DIR_NESTED = "nested";

    private static final String FILE_NO_EXTENSION = "afile";
    private static final String FILE_TXT_1 = "file-1.txt";
    private static final String FILE_TXT_2 = "file-2.txt";
    private static final String FILE_LOG = "file.log";
    private static final String FILE_NESTED_TXT = "file-3.txt";

    private static final String ARTIFACT_NAME = "test-out";
    private static final String TEST_CONTENT = "file content";

    private File tempDir;
    private File outputDir;
    private File baseDir;
    private PulseExecutionContext executionContext;
    private TestCommandContext commandContext;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        tempDir = createTempDirectory();
        baseDir = new File(tempDir, "base");
        assertTrue(baseDir.mkdir());
        outputDir = new File(tempDir, "output");

        Files.write(TEST_CONTENT, new File(baseDir, FILE_NO_EXTENSION), Charset.defaultCharset());
        Files.write(TEST_CONTENT, new File(baseDir, FILE_TXT_1), Charset.defaultCharset());
        Files.write(TEST_CONTENT, new File(baseDir, FILE_TXT_2), Charset.defaultCharset());
        Files.write(TEST_CONTENT, new File(baseDir, FILE_LOG), Charset.defaultCharset());

        File nested = new File(baseDir, DIR_NESTED);
        assertTrue(nested.mkdir());
        Files.write(TEST_CONTENT, new File(nested, FILE_NESTED_TXT), Charset.defaultCharset());

        executionContext = new PulseExecutionContext();
        executionContext.setWorkingDir(baseDir);
        executionContext.addString(NAMESPACE_INTERNAL, PROPERTY_OUTPUT_DIR, outputDir.getAbsolutePath());
        commandContext = new TestCommandContext(executionContext);
    }

    @Override
    protected void tearDown() throws Exception
    {
        removeDirectory(tempDir);
    }

    public void testRelativePath() throws IOException
    {
        capture(FILE_NO_EXTENSION);
        assertFileCaptured(FILE_NO_EXTENSION);
    }

    public void testNested() throws IOException
    {
        capture(DIR_NESTED + "/" + FILE_NESTED_TXT);
        assertFileCaptured(FILE_NESTED_TXT);
    }
    
    public void testAbsolutePath() throws IOException
    {
        File toCapture = new File(baseDir, FILE_NO_EXTENSION);
        capture(toCapture.getAbsolutePath());
        assertFileCaptured(FILE_NO_EXTENSION);
    }

    public void testSimpleWildcard() throws IOException
    {
        capture("*.log");
        assertFileCaptured(FILE_LOG);
    }

    public void testWildcardAndCurrentDirectoryAtStart() throws IOException
    {
        capture("./file*.log");
        assertFileCaptured(FILE_LOG);
    }

    public void testWildcardAndCurrentDirectoryWithin() throws IOException
    {
        capture(DIR_NESTED + "/./file*");
        assertFileCaptured(FILE_NESTED_TXT);
    }

    public void testWildcardAndParentDirectoryAtStart() throws IOException
    {
        capture("../" + baseDir.getName() + "/file*.log");
        assertFileCaptured(FILE_LOG);
    }

    public void testWildcardAndParentDirectoryWithin() throws IOException
    {
        capture(DIR_NESTED + "/../file*.log");
        assertFileCaptured(FILE_LOG);
    }

    public void testAbsoluteWithWildcard() throws IOException
    {
        File pattern = new File(baseDir, "*.log");
        capture(pattern.getAbsolutePath());
        assertFileCaptured(FILE_LOG);
    }

    public void testAbsoluteWithWildcardAfterName() throws IOException
    {
        File pattern = new File(baseDir, "file*.log");
        capture(pattern.getAbsolutePath());
        assertFileCaptured(FILE_LOG);
    }

    public void testWildcardNested() throws IOException
    {
        capture("nested/*.txt");
        assertFileCaptured(FILE_NESTED_TXT);
    }

    public void testWildcardMultipleFiles() throws IOException
    {
        capture("*.txt");
        assertFileCaptured(FILE_TXT_1);
        assertFileCaptured(FILE_TXT_2);
    }

    public void testWildcardMultipleFilesAcrossDirectories() throws IOException
    {
        capture("**/*.txt");
        assertFileCaptured(FILE_TXT_1);
        assertFileCaptured(FILE_TXT_2);
        assertFileCaptured(DIR_NESTED + "/" + FILE_NESTED_TXT);
    }

    public void testNonExistantFile()
    {
        nonExistantTest("nosuchfile");
    }

    public void testNonExistantFileAbsolutePath()
    {
        File nonExistant = new File(baseDir, "nosuchfile");
        nonExistantTest(nonExistant.getAbsolutePath());
    }

    public void testNonExistantFileWithWildcards()
    {
        nonExistantTest("**/*.exe");
    }

    public void testNonExistantFileAbsoluteWithWildcards()
    {
        File nonExistant = new File(baseDir, "**/*.exe");
        nonExistantTest(nonExistant.getAbsolutePath());
    }
    
    private void nonExistantTest(String path)
    {
        try
        {
            capture(path);
            fail("Fail if not present should cause an exception");
        }
        catch (BuildException e)
        {
            assertThat(e.getMessage(), containsString("no file matching"));
        }
    }

    public void testNonExistantFileNoFail()
    {
        nonExistantNoFailTest("nosuchfile");
    }

    public void testNonExistantFileNoFailAbsolutePath()
    {
        File nonExistant = new File(baseDir, "nosuchfile");
        nonExistantNoFailTest(nonExistant.getAbsolutePath());
    }

    public void testNonExistantFileNoFailWithWildcards()
    {
        nonExistantNoFailTest("**/*.exe");
    }

    public void testNonExistantFileNoFailAbsoluteWithWildcards()
    {
        File nonExistant = new File(baseDir, "**/*.exe");
        nonExistantNoFailTest(nonExistant.getAbsolutePath());
    }

    private void nonExistantNoFailTest(String path)
    {
        FileArtifactConfiguration config = new FileArtifactConfiguration(ARTIFACT_NAME, path);
        config.setFailIfNotPresent(false);

        capture(config);

        assertNoFilesCaptured();
    }

    public void testFileWithForwardSlashOnWindows()
    {
        if (SystemUtils.IS_WINDOWS)
        {
            nonExistantTest("/" + FILE_TXT_1);
        }
    }

    public void testAbsoluteFileWithForwardSlashOnWindows() throws IOException
    {
        if (SystemUtils.IS_WINDOWS)
        {
            File f = new File(baseDir, FILE_TXT_1);
            capture(f.getAbsolutePath().substring(2).replace('\\', '/'));

            assertFileCaptured(FILE_TXT_1);
        }
    }

    public void testFileStartingWithDot() throws IOException
    {
        capture("./" + FILE_TXT_1);
        assertFileCaptured(FILE_TXT_1);
    }

    public void testFileContainingDot() throws IOException
    {
        capture(DIR_NESTED + "/./" + FILE_NESTED_TXT);
        assertFileCaptured(FILE_NESTED_TXT);
    }

    public void testFileContainingDotDot() throws IOException
    {
        capture(DIR_NESTED + "/../" + FILE_LOG);
        assertFileCaptured(FILE_LOG);
    }

    public void testIgnoreStaleNotStale() throws IOException
    {
        FileArtifactConfiguration config = new FileArtifactConfiguration(ARTIFACT_NAME, FILE_LOG);
        config.setIgnoreStale(true);

        capture(config);

        assertFileCaptured(FILE_LOG);
    }

    public void testIgnoreStaleIsStale() throws IOException
    {
        staleFileTest(FILE_LOG);
    }

    public void testIgnoreStaleMultipleStaleFiles()
    {
        staleFileTest("**/*.txt");
    }

    private void staleFileTest(String path)
    {
        setRecipeTimestampInFuture();

        FileArtifactConfiguration config = new FileArtifactConfiguration(ARTIFACT_NAME, path);
        config.setIgnoreStale(true);

        try
        {
            capture(config);
            fail("Fail if not present should kick in if file is stale");
        }
        catch (BuildException e)
        {
            assertThat(e.getMessage(), containsString("stale"));
        }
    }

    public void testIgnoreStaleNoFailIsStale() throws IOException
    {
        staleFileNoFailTest(FILE_LOG);
    }

    public void testIgnoreStaleNoFailMultipleStaleFiles()
    {
        staleFileNoFailTest("**/*.txt");
    }

    private void staleFileNoFailTest(String path)
    {
        setRecipeTimestampInFuture();

        FileArtifactConfiguration config = new FileArtifactConfiguration(ARTIFACT_NAME, path);
        config.setFailIfNotPresent(false);
        config.setIgnoreStale(true);

        capture(config);

        assertNoFilesCaptured();
    }
    
    private void setRecipeTimestampInFuture()
    {
        executionContext.addValue(BuildProperties.NAMESPACE_INTERNAL, BuildProperties.PROPERTY_RECIPE_TIMESTAMP_MILLIS, System.currentTimeMillis() + 1);
    }

    private void capture(String path)
    {
        FileArtifactConfiguration config = new FileArtifactConfiguration(ARTIFACT_NAME, path);
        capture(config);
    }

    private void capture(FileArtifactConfiguration config)
    {
        FileArtifact artifact = new FileArtifact(config);
        artifact.capture(commandContext);
    }

    private void assertFileCaptured(String fileName) throws IOException
    {
        Map<String, TestCommandContext.Artifact> artifacts = commandContext.getArtifacts();
        assertNotNull(artifacts.get(ARTIFACT_NAME));

        File capturedFile = new File(outputDir, FileSystemUtils.composeFilename(ARTIFACT_NAME, fileName));
        assertTrue(capturedFile.isFile());
        assertEquals(TEST_CONTENT, Files.toString(capturedFile, Charset.defaultCharset()));
    }

    private void assertNoFilesCaptured()
    {
        File dir = new File(outputDir, ARTIFACT_NAME);
        assertEquals(0, dir.list().length);
    }
}
