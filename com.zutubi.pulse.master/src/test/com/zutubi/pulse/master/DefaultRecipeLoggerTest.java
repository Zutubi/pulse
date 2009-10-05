package com.zutubi.pulse.master;

import static com.zutubi.pulse.core.test.api.Matchers.matchesRegex;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

public class DefaultRecipeLoggerTest extends PulseTestCase
{
    private static final String LINE_ENDING_UNIX = "\n";
    private static final String LINE_ENDING_WINDOWS = "\r\n";
    private static final String LINE_ENDING_MAC = "\r";

    private File tmpDir;
    private File logFile;
    private DefaultRecipeLogger logger;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        tmpDir = FileSystemUtils.createTempDir(getName(), ".tmp");
        logFile = new File(tmpDir, "file.log");
        logger = new DefaultRecipeLogger(logFile);
        logger.prepare();
    }

    @Override
    protected void tearDown() throws Exception
    {
        logger.close();
        removeDirectory(tmpDir);
        super.tearDown();
    }
    
    public void testOutputFlushed() throws IOException
    {
        final String SHORT_STRING = "1";

        logger.log(SHORT_STRING.getBytes());
        assertTrue(IOUtils.fileToString(logFile).length() > SHORT_STRING.length());
    }

    public void testOffsetAndLength() throws IOException
    {
        logger.log("0123456789".getBytes("US-ASCII"), 2, 4);
        assertLineContent(IOUtils.fileToString(logFile), "2345");
    }

    public void testNewlineOnBoundaryUnix() throws IOException
    {
        newlineOnBoundaryHelper(LINE_ENDING_UNIX);
    }

    public void testNewlineOnBoundaryWindows() throws IOException
    {
        newlineOnBoundaryHelper(LINE_ENDING_WINDOWS);
    }

    public void testNewlineOnBoundaryMac() throws IOException
    {
        newlineOnBoundaryHelper(LINE_ENDING_MAC);
    }
    
    private void newlineOnBoundaryHelper(String lineEnding) throws IOException
    {
        logger.log(("line 1" + lineEnding).getBytes());
        logger.log("line 2".getBytes());

        assertLines(2);
    }

    public void testNewlineStraddlesBoundary() throws IOException
    {
        logger.log(("line 1\r").getBytes());
        logger.log("\nline 2".getBytes());

        assertLines(2);
    }

    public void testLineNotSplitUnix() throws IOException
    {
        lineNotSplitHelper(LINE_ENDING_UNIX);
    }

    public void testLineNotSplitWindows() throws IOException
    {
        lineNotSplitHelper(LINE_ENDING_WINDOWS);
    }

    public void testLineNotSplitMac() throws IOException
    {
        lineNotSplitHelper(LINE_ENDING_MAC);
    }

    private void lineNotSplitHelper(String lineEnding) throws IOException
    {
        // CIB-1782
        logger.log(("line 1" + lineEnding + "lin").getBytes());
        logger.log(("e 2" + lineEnding + "line 3").getBytes());

        assertLines(3);
    }

    public void testNewlineAtStartOfOutputUnix() throws IOException
    {
        newlineAtStartOfOutputHelper(LINE_ENDING_UNIX);
    }

    public void testNewlineAtStartOfOutputWindows() throws IOException
    {
        newlineAtStartOfOutputHelper(LINE_ENDING_WINDOWS);
    }

    public void testNewlineAtStartOfOutputMac() throws IOException
    {
        newlineAtStartOfOutputHelper(LINE_ENDING_MAC);
    }

    private void newlineAtStartOfOutputHelper(String lineEnding) throws IOException
    {
        logger.writePreRule();
        logger.log((lineEnding + "line 1" + lineEnding + "line 2").getBytes());

        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new FileReader(logFile));
            assertEquals(DefaultRecipeLogger.PRE_RULE, reader.readLine());
            reader.readLine();
            readLines(2, reader);
            assertNull(reader.readLine());
        }
        finally
        {
            IOUtils.close(reader);
        }
    }

    public void testNewlineAtEndOfOutputUnix() throws IOException
    {
        newlineAtEndOfOutputHelper(LINE_ENDING_UNIX);
    }

    public void testNewlineAtEndOfOutputWindows() throws IOException
    {
        newlineAtEndOfOutputHelper(LINE_ENDING_WINDOWS);
    }

    public void testNewlineAtEndOfOutputMac() throws IOException
    {
        newlineAtEndOfOutputHelper(LINE_ENDING_MAC);
    }

    private void newlineAtEndOfOutputHelper(String lineEnding) throws IOException
    {
        logger.log(("line 1" + lineEnding + "line 2" + lineEnding).getBytes());
        logger.writePostRule();

        assertEndOfOutput();
    }

    public void testNoNewlineAtEndOfOutputUnix() throws IOException
    {
        noNewlineAtEndOfOutputHelper(LINE_ENDING_UNIX);
    }

    public void testNoNewlineAtEndOfOutputWindows() throws IOException
    {
        noNewlineAtEndOfOutputHelper(LINE_ENDING_WINDOWS);
    }

    public void testNoNewlineAtEndOfOutputMac() throws IOException
    {
        noNewlineAtEndOfOutputHelper(LINE_ENDING_MAC);
    }

    private void noNewlineAtEndOfOutputHelper(String lineEnding) throws IOException
    {
        logger.log(("line 1" + lineEnding + "line 2").getBytes());
        logger.writePostRule();

        assertEndOfOutput();
    }

    public void testReopenSameLogFile() throws IOException
    {
        logger.logMarker("First line");
        logger.close();
        logger = new DefaultRecipeLogger(logFile);
        logger.prepare();
        logger.logMarker("Second line");

        String content = IOUtils.fileToString(logFile);
        assertThat(content, containsString("First line"));
        assertThat(content, containsString("Second line"));
    }

    private void assertEndOfOutput() throws IOException
    {
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new FileReader(logFile));
            readLines(2, reader);
            assertEquals(DefaultRecipeLogger.POST_RULE, reader.readLine());
            assertNull(reader.readLine());
        }
        finally
        {
            IOUtils.close(reader);
        }
    }

    private void assertLines(int count) throws IOException
    {
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new FileReader(logFile));
            readLines(count, reader);
            assertNull(reader.readLine());
        }
        finally
        {
            IOUtils.close(reader);
        }
    }

    private void readLines(int count, BufferedReader reader) throws IOException
    {
        for (int i = 1; i <= count; i++)
        {
            assertLineContent(reader.readLine(), "line " + i);
        }
    }

    private void assertLineContent(String line, String content)
    {
        assertThat(line, matchesRegex("[0-9][0-9]?/[0-9][0-9]/[0-9][0-9] [0-9][0-9]?:[0-9][0-9]:[0-9][0-9][A-Z ]*: " + Pattern.quote(content)));
    }
}
