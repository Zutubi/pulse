package com.zutubi.pulse.master.project;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.project.events.ProjectStatusEvent;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.StringUtils;
import static com.zutubi.util.io.IOUtils.*;

import java.io.File;
import java.io.IOException;

public class ProjectLoggerTest extends PulseTestCase
{
    private static final String TEST_MESSAGE = "test message";
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final int    LINE_LIMIT   = 15;

    private File tempDir;
    private ProjectLogger logger;

    protected void setUp() throws Exception
    {
        super.setUp();
        tempDir = FileSystemUtils.createTempDir(getName(), ".tmp");

        // 19 for the timestamp prefix
        int lineLength = 21 + TEST_MESSAGE.length() + LINE_SEPARATOR.length();
        logger = new ProjectLogger(tempDir, lineLength * LINE_LIMIT);
    }

    protected void tearDown() throws Exception
    {
        close(logger);
        removeDirectory(tempDir);

        super.tearDown();
    }

    public void testWritesToLogFile() throws IOException
    {
        logMessage(TEST_MESSAGE);
        logger.close();
        // Null out to prevent second close in tearDown.
        logger = null;

        String written = fileToString(new File(tempDir, String.format(ProjectLogger.NAME_PATTERN, 0)));
        assertEquals(TEST_MESSAGE + LINE_SEPARATOR, wipeTimestamps(written));
    }

    public void testTail() throws IOException
    {
        for (int i = 0; i < 10; i++)
        {
            logMessage(TEST_MESSAGE + i);
        }

        String tail = wipeTimestamps(logger.tail(3));
        assertEquals(TEST_MESSAGE + "7\n" + TEST_MESSAGE + "8\n" + TEST_MESSAGE + "9\n", tail);
    }

    public void testRotatesAfterLimit() throws IOException
    {
        for(int i = 0; i < LINE_LIMIT; i++)
        {
            logMessage(TEST_MESSAGE);
        }

        File rotatedFile = new File(tempDir, String.format(ProjectLogger.NAME_PATTERN, 1));
        assertFalse(rotatedFile.exists());

        logMessage(TEST_MESSAGE);
        assertTrue(rotatedFile.exists());

        assertEquals(getFullContent(), wipeTimestamps(fileToString(rotatedFile)));

        File primaryFile = new File(tempDir, String.format(ProjectLogger.NAME_PATTERN, 0));
        assertEquals(TEST_MESSAGE + LINE_SEPARATOR, wipeTimestamps(fileToString(primaryFile)));
    }

    public void testTailAfterRotate() throws IOException
    {
        String expectedTail = "";
        for(int i = 0; i < LINE_LIMIT + 2; i++)
        {
            String line = TEST_MESSAGE + i;
            logMessage(line);
            if (i >= 2)
            {
                expectedTail += line + "\n";
            }
        }

        String tail = logger.tail(LINE_LIMIT);
        assertEquals(expectedTail, wipeTimestamps(tail));
    }

    public void testRotateTwice() throws IOException
    {
        for (int i = 0; i < LINE_LIMIT * 2 + 1; i++)
        {
            logMessage(TEST_MESSAGE);
        }

        File primaryFile = new File(tempDir, String.format(ProjectLogger.NAME_PATTERN, 0));
        assertEquals(TEST_MESSAGE + LINE_SEPARATOR, wipeTimestamps(fileToString(primaryFile)));

        File rotatedFile = new File(tempDir, String.format(ProjectLogger.NAME_PATTERN, 1));
        assertEquals(getFullContent(), wipeTimestamps(fileToString(rotatedFile)));
    }

    public void testRawWithNoLogMessages() throws IOException
    {
        assertEquals("", inputStreamToString(logger.getRawInputStream()));
    }

    public void testRaw() throws IOException
    {
        rawHelper(1, getSingleLine());
    }

    public void testRawAfterFirstRotation() throws IOException
    {
        rawHelper(LINE_LIMIT + 1, getFullContent() + getSingleLine());
    }

    public void testRawAfterSecondRotation() throws IOException
    {
        rawHelper(LINE_LIMIT * 2 + 1, getFullContent() + getSingleLine());
    }

    public void testRawBeforeSecondRotation() throws IOException
    {
        rawHelper(LINE_LIMIT * 2, getFullContent() + getFullContent());
    }

    private void rawHelper(int numLines, String expected) throws IOException
    {
        for (int i = 0; i < numLines; i++)
        {
            logMessage(TEST_MESSAGE);
        }

        String raw = inputStreamToString(logger.getRawInputStream());
        assertEquals(expected, wipeTimestamps(raw));
    }

    private void logMessage(String message)
    {
        logger.log(new ProjectStatusEvent(this, null, message));
    }

    private String wipeTimestamps(String tail)
    {
        return tail.replaceAll("(?m)^.*: ", "");
    }

    private String getSingleLine()
    {
        return TEST_MESSAGE + LINE_SEPARATOR;
    }

    private String getFullContent()
    {
        return StringUtils.join(LINE_SEPARATOR, CollectionUtils.times(TEST_MESSAGE, LINE_LIMIT)) + LINE_SEPARATOR;
    }
}
