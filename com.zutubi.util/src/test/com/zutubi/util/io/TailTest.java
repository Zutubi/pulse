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
        singleFileHelper(100, 5);
    }

    public void testZeroLines() throws IOException
    {
        singleFileHelper(100, 0);
    }

    public void testMoreLinesThanInFile() throws IOException
    {
        singleFileHelper(5, 10);
    }

    public void testZeroLengthFile() throws IOException
    {
        singleFileHelper(0, 10);
    }

    public void testLineLengthFourUnderEstimate() throws IOException
    {
        singleFileHelper(20, 10, Tail.ESTIMATED_BYTES_PER_LINE - 4);
    }

    public void testLineLengthThreeUnderEstimate() throws IOException
    {
        singleFileHelper(20, 10, Tail.ESTIMATED_BYTES_PER_LINE - 3);
    }

    public void testLineLengthTwoUnderEstimate() throws IOException
    {
        singleFileHelper(20, 10, Tail.ESTIMATED_BYTES_PER_LINE - 2);
    }

    public void testLineLengthOneUnderEstimate() throws IOException
    {
        singleFileHelper(20, 10, Tail.ESTIMATED_BYTES_PER_LINE - 1);
    }

    public void testLineLengthEqualsEstimate() throws IOException
    {
        singleFileHelper(20, 10, Tail.ESTIMATED_BYTES_PER_LINE);
    }

    public void testLineLengthOneOverEstimate() throws IOException
    {
        singleFileHelper(20, 10, Tail.ESTIMATED_BYTES_PER_LINE + 1);
    }

    public void testLineLengthTwoOverEstimate() throws IOException
    {
        singleFileHelper(20, 10, Tail.ESTIMATED_BYTES_PER_LINE + 2);
    }

    public void testLineLengthThreeOverEstimate() throws IOException
    {
        singleFileHelper(20, 10, Tail.ESTIMATED_BYTES_PER_LINE + 3);
    }

    public void testLineLengthFourOverEstimate() throws IOException
    {
        singleFileHelper(20, 10, Tail.ESTIMATED_BYTES_PER_LINE + 4);
    }

    public void testLineLengthFourUnderChunkSize() throws IOException
    {
        singleFileHelper(20, 10, Tail.ESTIMATED_BYTES_PER_LINE * 10 - 4);
    }

    public void testLineLengthThreeUnderChunkSize() throws IOException
    {
        singleFileHelper(20, 10, Tail.ESTIMATED_BYTES_PER_LINE * 10 - 3);
    }

    public void testLineLengthTwoUnderChunkSize() throws IOException
    {
        singleFileHelper(20, 10, Tail.ESTIMATED_BYTES_PER_LINE * 10 - 2);
    }

    public void testLineLengthOneUnderChunkSize() throws IOException
    {
        singleFileHelper(20, 10, Tail.ESTIMATED_BYTES_PER_LINE * 10 - 1);
    }

    public void testLineLengthEqualToChunkSize() throws IOException
    {
        singleFileHelper(20, 10, Tail.ESTIMATED_BYTES_PER_LINE * 10);
    }

    public void testLineLengthOneOverChunkSize() throws IOException
    {
        singleFileHelper(20, 10, Tail.ESTIMATED_BYTES_PER_LINE * 10 + 1);
    }

    public void testLineLengthTwoOverChunkSize() throws IOException
    {
        singleFileHelper(20, 10, Tail.ESTIMATED_BYTES_PER_LINE * 10 + 2);
    }

    public void testLineLengthThreeOverChunkSize() throws IOException
    {
        singleFileHelper(20, 10, Tail.ESTIMATED_BYTES_PER_LINE * 10 + 3);
    }

    public void testLineLengthFourOverChunkSize() throws IOException
    {
        singleFileHelper(20, 10, Tail.ESTIMATED_BYTES_PER_LINE * 10 + 4);
    }

    public void testLineLengthGreaterThanToChunkSize() throws IOException
    {
        singleFileHelper(20, 10, Tail.ESTIMATED_BYTES_PER_LINE * 11);
    }

    public void testTwoFilesHalfRequired() throws IOException
    {
        multipleFileHelper(2, 20, 10);
    }

    public void testTwoFilesExactlyOneRequired() throws IOException
    {
        multipleFileHelper(2, 20, 20);
    }

    public void testTwoFilesJustMoreThanOneRequired() throws IOException
    {
        multipleFileHelper(2, 20, 21);
    }

    public void testTwoFilesOneAndAHalfRequired() throws IOException
    {
        multipleFileHelper(2, 20, 30);
    }

    public void testTwoFilesTwoRequired() throws IOException
    {
        multipleFileHelper(2, 20, 40);
    }

    public void testTwoFilesOverTwoRequired() throws IOException
    {
        multipleFileHelper(2, 20, 50);
    }

    public void testThreeFilesHalfRequired() throws IOException
    {
        multipleFileHelper(3, 20, 10);
    }

    public void testThreeFilesJustUnderTwoRequired() throws IOException
    {
        multipleFileHelper(3, 20, 39);
    }

    public void testThreeFilesExactlyTwoRequired() throws IOException
    {
        multipleFileHelper(3, 20, 40);
    }

    public void testThreeFilesJustOverTwoRequired() throws IOException
    {
        multipleFileHelper(3, 20, 41);
    }
    
    public void testThreeFilesTwoAndAHalfRequired() throws IOException
    {
        multipleFileHelper(3, 20, 50);
    }

    public void testThreeFilesThreeRequired() throws IOException
    {
        multipleFileHelper(3, 20, 60);
    }

    public void testThreeFilesExactlyOverThreeRequired() throws IOException
    {
        multipleFileHelper(3, 20, 70);
    }

    private void singleFileHelper(int lineCount, int tailLines) throws IOException
    {
        singleFileHelper(lineCount, tailLines, 0);
    }

    private void singleFileHelper(int lineCount, int tailLines, long lineLength) throws IOException
    {
        File f = generateTestFile(lineCount, lineLength, 0);
        Tail tail = new Tail(tailLines, f);
        assertEquals(getExpectedTail(lineCount, tailLines, lineLength), tail.getTail());
    }

    private void multipleFileHelper(int fileCount, int linesPerFile, int tailLines) throws IOException
    {
        File[] files = new File[fileCount];
        for (int i = 0; i < fileCount; i++)
        {
            files[i] = generateTestFile(linesPerFile, 0, i * linesPerFile);
        }

        Tail tail = new Tail(tailLines, files);
        assertEquals(getExpectedTail(fileCount * linesPerFile, tailLines, 0), tail.getTail());

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
        return generateTestFile(lineCount, 0, 0);
    }

    private File generateTestFile(long lineCount, long lineLength, long offset) throws IOException
    {
        File file = createRandomFile();

        BufferedWriter out = null;
        try
        {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            for (long l = 0; l < lineCount; l++)
            {
                out.write(getLine(l + offset, lineLength));
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
