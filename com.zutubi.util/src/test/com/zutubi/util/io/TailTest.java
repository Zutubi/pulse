package com.zutubi.util.io;

import com.google.common.io.Files;
import com.zutubi.util.StringUtils;
import com.zutubi.util.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

public class TailTest extends BaseIOTestCase
{
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

    public void testNoNewlinesInFile() throws IOException
    {
        File f = createRandomFile();
        Files.append("abcdef", f, Charset.defaultCharset());

        Tail tail = new Tail(10, f);
        assertEquals("abcdef", tail.getTail());
    }

    public void testNoNewlineAtEndOfFileUnderMaxLines() throws IOException
    {
        File f = createRandomFile();
        String lines = StringUtils.join(SystemUtils.LINE_SEPARATOR, Arrays.asList("1", "2", "3"));
        Files.append(lines, f, Charset.defaultCharset());

        Tail tail = new Tail(4, f);
        assertEquals(lines.replaceAll("\\r", ""), tail.getTail());
    }

    public void testNoNewlineAtEndOfFileOverMaxLines() throws IOException
    {
        File f = createRandomFile();
        String lines = StringUtils.join(SystemUtils.LINE_SEPARATOR, Arrays.asList("1", "2", "3", "4", "5"));
        Files.append(lines, f, Charset.defaultCharset());

        Tail tail = new Tail(4, f);
        assertEquals(StringUtils.join("\n", Arrays.asList("2", "3", "4", "5")), tail.getTail());
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
}
