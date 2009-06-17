package com.zutubi.pulse.core.commands.api;

import com.zutubi.pulse.core.PulseExecutionContext;
import static com.zutubi.pulse.core.engine.api.BuildProperties.NAMESPACE_INTERNAL;
import static com.zutubi.pulse.core.engine.api.BuildProperties.PROPERTY_OUTPUT_DIR;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class FileOutputTest extends PulseTestCase
{
    private static final String OUTPUT_NAME = "test-out";
    private static final String TEST_CONTENT = "file content";

    private File tempDir;
    private File outputDir;
    private File baseDir;
    private TestCommandContext commandContext;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        tempDir = FileSystemUtils.createTempDir(getName(), ".tmp");
        baseDir = new File(tempDir, "base");
        assertTrue(baseDir.mkdir());
        outputDir = new File(tempDir, "output");

        PulseExecutionContext executionContext = new PulseExecutionContext();
        executionContext.setWorkingDir(baseDir);
        executionContext.addString(NAMESPACE_INTERNAL, PROPERTY_OUTPUT_DIR, outputDir.getAbsolutePath());
        commandContext = new TestCommandContext(executionContext);
    }

    @Override
    protected void tearDown() throws Exception
    {
        FileSystemUtils.rmdir(tempDir);
    }

    public void testRelativePath() throws IOException
    {
        final String FILE_NAME = "foo";

        File toCapture = new File(baseDir, FILE_NAME);
        FileSystemUtils.createFile(toCapture, TEST_CONTENT);

        capture(FILE_NAME);
        assertFileCaptured(FILE_NAME);
    }

    public void testAbsolutePath() throws IOException
    {
        final String FILE_NAME = "foo";

        File toCapture = new File(tempDir, FILE_NAME);
        FileSystemUtils.createFile(toCapture, TEST_CONTENT);

        capture(toCapture.getAbsolutePath());

        assertFileCaptured(FILE_NAME);
    }

    public void testSimpleWildcard() throws IOException
    {
        final String FILE_NAME = "file-3.txt";

        File toCapture = new File(baseDir, FILE_NAME);
        FileSystemUtils.createFile(toCapture, TEST_CONTENT);

        capture("file-*.txt");
        assertFileCaptured(FILE_NAME);
    }

    private void capture(String path)
    {
        FileOutputConfiguration config = new FileOutputConfiguration(OUTPUT_NAME, path);
        FileOutput output = new FileOutput(config);
        output.capture(commandContext);
    }

    private void assertFileCaptured(String fileName) throws IOException
    {
        Map<String,TestCommandContext.Output> outputs = commandContext.getOutputs();
        assertNotNull(outputs.get(OUTPUT_NAME));

        File capturedFile = new File(outputDir, FileSystemUtils.composeFilename(OUTPUT_NAME, fileName));
        assertTrue(capturedFile.isFile());
        assertEquals(TEST_CONTENT, IOUtils.fileToString(capturedFile));
    }
}
