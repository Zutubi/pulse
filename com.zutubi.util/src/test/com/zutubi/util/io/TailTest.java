package com.zutubi.util.io;

import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.RandomUtils;
import junit.framework.TestCase;

import java.io.*;

public class TailTest extends TestCase
{
    private File tmp;

    protected void setUp() throws Exception
    {
        super.setUp();
        tmp = FileSystemUtils.createTempDir();
    }

    protected void tearDown() throws Exception
    {
        FileSystemUtils.rmdir(tmp);
        super.tearDown();
    }

    public void testDefaultTail() throws IOException
    {
        File f = generateTestFile(100);
        Tail tail = new Tail(f);
        assertEquals(getExpectedTail(100, 10, 0), tail.getTail());
    }

    public void testCustomLines() throws IOException
    {
        generateAndTail(100, 5);
    }

    public void testZeroLines() throws IOException
    {
        generateAndTail(100, 0);
    }

    public void testMoreLinesThanInFile() throws IOException
    {
        generateAndTail(5, 10);
    }

    public void testZeroLengthFile() throws IOException
    {
        generateAndTail(0, 10);
    }

    public void testLineLengthFourUnderEstimate() throws IOException
    {
        generateAndTail(20, 10, Tail.ESTIMATED_BYTES_PER_LINE - 4);
    }

    public void testLineLengthThreeUnderEstimate() throws IOException
    {
        generateAndTail(20, 10, Tail.ESTIMATED_BYTES_PER_LINE - 3);
    }

    public void testLineLengthTwoUnderEstimate() throws IOException
    {
        generateAndTail(20, 10, Tail.ESTIMATED_BYTES_PER_LINE - 2);
    }

    public void testLineLengthOneUnderEstimate() throws IOException
    {
        generateAndTail(20, 10, Tail.ESTIMATED_BYTES_PER_LINE - 1);
    }

    public void testLineLengthEqualsEstimate() throws IOException
    {
        generateAndTail(20, 10, Tail.ESTIMATED_BYTES_PER_LINE);
    }

    public void testLineLengthOneOverEstimate() throws IOException
    {
        generateAndTail(20, 10, Tail.ESTIMATED_BYTES_PER_LINE + 1);
    }

    public void testLineLengthTwoOverEstimate() throws IOException
    {
        generateAndTail(20, 10, Tail.ESTIMATED_BYTES_PER_LINE + 2);
    }

    public void testLineLengthThreeOverEstimate() throws IOException
    {
        generateAndTail(20, 10, Tail.ESTIMATED_BYTES_PER_LINE + 3);
    }

    public void testLineLengthFourOverEstimate() throws IOException
    {
        generateAndTail(20, 10, Tail.ESTIMATED_BYTES_PER_LINE + 4);
    }

    public void testLineLengthFourUnderChunkSize() throws IOException
    {
        generateAndTail(20, 10, Tail.ESTIMATED_BYTES_PER_LINE * 10 - 4);
    }

    public void testLineLengthThreeUnderChunkSize() throws IOException
    {
        generateAndTail(20, 10, Tail.ESTIMATED_BYTES_PER_LINE * 10 - 3);
    }

    public void testLineLengthTwoUnderChunkSize() throws IOException
    {
        generateAndTail(20, 10, Tail.ESTIMATED_BYTES_PER_LINE * 10 - 2);
    }

    public void testLineLengthOneUnderChunkSize() throws IOException
    {
        generateAndTail(20, 10, Tail.ESTIMATED_BYTES_PER_LINE * 10 - 1);
    }

    public void testLineLengthEqualToChunkSize() throws IOException
    {
        generateAndTail(20, 10, Tail.ESTIMATED_BYTES_PER_LINE * 10);
    }

    public void testLineLengthOneOverChunkSize() throws IOException
    {
        generateAndTail(20, 10, Tail.ESTIMATED_BYTES_PER_LINE * 10 + 1);
    }

    public void testLineLengthTwoOverChunkSize() throws IOException
    {
        generateAndTail(20, 10, Tail.ESTIMATED_BYTES_PER_LINE * 10 + 2);
    }

    public void testLineLengthThreeOverChunkSize() throws IOException
    {
        generateAndTail(20, 10, Tail.ESTIMATED_BYTES_PER_LINE * 10 + 3);
    }

    public void testLineLengthFourOverChunkSize() throws IOException
    {
        generateAndTail(20, 10, Tail.ESTIMATED_BYTES_PER_LINE * 10 + 4);
    }

    public void testLineLengthGreaterThanToChunkSize() throws IOException
    {
        generateAndTail(20, 10, Tail.ESTIMATED_BYTES_PER_LINE * 11);
    }

    private void generateAndTail(int lineCount, int tailLines) throws IOException
    {
        generateAndTail(lineCount, tailLines, 0);
    }

    private void generateAndTail(int lineCount, int tailLines, long lineLength) throws IOException
    {
        File f = generateTestFile(lineCount, lineLength);
        Tail tail = new Tail(f, tailLines);
        assertEquals(getExpectedTail(lineCount, tailLines, lineLength), tail.getTail());
    }

    private String getExpectedTail(int lineCount, int tailLines, long lineLength)
    {
        if (tailLines > lineCount)
        {
            tailLines = lineCount;
        }

        StringBuilder builder = new StringBuilder();
        for (long i = lineCount - tailLines; i < lineCount; i++)
        {
            builder.append(getLine(i, lineLength));
            builder.append('\n');
        }

        return builder.toString();
    }

    /**
     * Generate a new randomly named test file that contains the line number on each line.
     *
     * @param lineCount in lines for this file.
     *
     * @return a randomly named file
     *
     * @throws IOException is raised if we have problems writing to the file.
     */
    private File generateTestFile(long lineCount) throws IOException
    {
        return generateTestFile(lineCount, 0);
    }

    private File generateTestFile(long lineCount, long lineLength) throws IOException
    {
        File file = createRandomFile();

        BufferedWriter out = null;
        try
        {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            for (long l = 0; l < lineCount; l++)
            {
                out.write(getLine(l, lineLength));
                out.newLine();
            }
        }
        finally
        {
            IOUtils.close(out);
        }
        return file;
    }

    private String getLine(long number, long lineLength)
    {
        StringBuilder builder = new StringBuilder();
        builder.append(Long.toString(number));
        while (builder.length() < lineLength)
        {
            builder.append('-');
        }

        return builder.toString();
    }

    private File createRandomFile() throws IOException
    {
        String randomName = RandomUtils.randomString(10);
        File file = new File(tmp, randomName);
        while (file.exists())
        {
            randomName = RandomUtils.randomString(10);
            file = new File(tmp, randomName);
        }

        if (!file.createNewFile())
        {
            throw new IOException("Unexpected failure to create temporary file.");
        }
        return file;
    }
}
