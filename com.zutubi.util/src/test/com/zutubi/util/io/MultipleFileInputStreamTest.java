package com.zutubi.util.io;

import com.zutubi.util.Constants;

import java.io.File;
import java.io.IOException;

public class MultipleFileInputStreamTest extends BaseIOTestCase
{
    private int lsl;
    private String ls;
    private MultipleFileInputStream input;

    protected void setUp() throws Exception
    {
        super.setUp();
        ls = Constants.LINE_SEPARATOR;
        lsl = ls.length();
    }

    protected void tearDown() throws Exception
    {
        IOUtils.close(input);
        super.tearDown();
    }

    public void testSingleFileAvailable() throws IOException
    {
        availableHelper(1, 10, 10);
    }

    public void testMultipleFileAvaialble() throws IOException
    {
        availableHelper(2, 20, 20);
        availableHelper(3, 30, 30);
        availableHelper(4, 40, 40);
    }

    public void testSingleFileAvailableAfterFullRead() throws IOException
    {
        readHelper(1, 10, 10, 100 + 10 * lsl);
        assertEquals(-1, input.available());
    }

    public void testMultipleFileAvailableAfterFullRead() throws IOException
    {
        readHelper(2, 10, 10, 200 + 20 * lsl);
        assertEquals(-1, input.available());
    }

    public void testReadSingleFile() throws IOException
    {
        readHelper(1, 10, 10, 10 + lsl);
    }

    public void testReadSingleFilePastEnd() throws IOException
    {
        readHelper(1, 10, 10, 50);
    }

    public void testReadSingleFileInOneGo() throws IOException
    {
        readHelper(1, 10, 10, 200);
    }

    public void testReadMultipleFile() throws IOException
    {
        readHelper(3, 10, 10, 10 + lsl);
    }

    public void testReadMultipleFileAcrossFileBoundry() throws IOException
    {
        readHelper(3, 10, 10, 100);
    }

    public void testReadMultipleFilesInOneGo() throws IOException
    {
        readHelper(3, 10, 10, 1000);
    }

    public void testReadContent() throws IOException
    {
        multipleFileHelper(1, 10, 10);
        assertBufferedRead(10 + lsl, "0---------" + ls);
        assertBufferedRead(90 +  9 * lsl, "1---------" + ls +
                "2---------" + ls +"3---------" + ls +
                "4---------" + ls +"5---------" + ls +
                "6---------" + ls +"7---------" + ls +
                "8---------" + ls +"9---------" + ls );
    }

    public void testReadContentAcrossFiles() throws IOException
    {
        multipleFileHelper(4, 1, 1);
        assertBufferedRead(4 + 4 * lsl, "0" + ls + "1" + ls + "2" + ls + "3" + ls);
    }

    public void testReadZeroData() throws IOException
    {
        File f = createRandomFile();
        input = new MultipleFileInputStream(f);
        assertEquals(-1, input.read());
    }

    public void testRead() throws IOException
    {
        multipleFileHelper(1, 1, 1);
        assertRead("0" + ls);
    }

    public void testReadAcrossFileBoundry() throws IOException
    {
        multipleFileHelper(3, 1, 1);
        assertRead("0" + ls + "1" + ls + "2" + ls);
    }

    public void testReadAcrossEmptyFiles() throws IOException
    {
        File[] files = new File[4];
        files[0] = generateTestFile(1, 1, 0);
        files[1] = generateTestFile(1, 1, 1);
        files[2] = createRandomFile();
        files[3] = generateTestFile(1, 1, 2);
        input = new MultipleFileInputStream(files);

        assertRead("0" + ls + "1" + ls + "2" + ls);
    }

    public void testBufferedReadAcrossFilesWithEmptyFile() throws IOException
    {
        File[] files = new File[4];
        files[0] = generateTestFile(1, 1, 0);
        files[1] = generateTestFile(1, 1, 1);
        files[2] = createRandomFile();
        files[3] = generateTestFile(1, 1, 2);
        input = new MultipleFileInputStream(files);

        String expected = "0" + ls + "1" + ls + "2" + ls;
        assertBufferedRead(expected.length(), expected);
    }

    public void testBufferedReadAcrossFilesWithEmptyFileAtStart() throws IOException
    {
        File[] files = new File[4];
        files[0] = createRandomFile();
        files[1] = generateTestFile(1, 1, 0);
        files[2] = generateTestFile(1, 1, 1);
        files[3] = generateTestFile(1, 1, 2);
        input = new MultipleFileInputStream(files);

        String expected = "0" + ls + "1" + ls + "2" + ls;
        assertBufferedRead(expected.length(), expected);
    }

    public void testBufferedReadAcrossFilesWithEmptyFileAtEnd() throws IOException
    {
        File[] files = new File[4];
        files[0] = generateTestFile(1, 1, 0);
        files[1] = generateTestFile(1, 1, 1);
        files[2] = generateTestFile(1, 1, 2);
        files[3] = createRandomFile();
        input = new MultipleFileInputStream(files);

        String expected = "0" + ls + "1" + ls + "2" + ls;
        assertBufferedRead(expected.length(), expected);
    }

    private void assertRead(String expected) throws IOException
    {
        for (byte b : expected.getBytes())
        {
            assertEquals((int) b, input.read());
        }
        assertEquals(-1, input.read());
    }

    public void testCreateWithZeroFiles() throws IOException
    {
        input = new MultipleFileInputStream();
    }

    public void testCreateWithNullFiles() throws IOException
    {
        input = new MultipleFileInputStream(null, null);
    }

    private void assertBufferedRead(int readbuffer, String expected) throws IOException
    {
        byte[] b = new byte[readbuffer];
        assertEquals(expected.length(), input.read(b));
        assertEquals(expected, new String(b));
    }

    private void availableHelper(int fileCount, int lineLength, int linesPerFile) throws IOException
    {
        MultipleFileInputStream input = multipleFileHelper(fileCount, lineLength, linesPerFile);
        assertEquals(fileCount * (lineLength + lsl) * linesPerFile,  input.available());
    }

    private void readHelper(int fileCount, int lineLength, int linesPerFile, int readSize) throws IOException
    {
        MultipleFileInputStream input = multipleFileHelper(fileCount, lineLength, linesPerFile);

        byte[] b = new byte[readSize];

        int expectedTotal = fileCount * (lineLength + lsl) * linesPerFile;

        for (int i = 0; i < expectedTotal/readSize; i++)
        {
            assertEquals(b.length, input.read(b));
        }
        
        int remaining = expectedTotal%readSize;
        if (remaining != 0)
        {
            assertEquals(remaining, input.read(b));
        }
        assertEquals(-1, input.read(b));

    }

    private MultipleFileInputStream multipleFileHelper(int fileCount, int lineLength, int linesPerFile) throws IOException
    {
        IOUtils.close(input);
        File[] files = new File[fileCount];
        for (int i = 0; i < fileCount; i++)
        {
            files[i] = generateTestFile(linesPerFile, lineLength, i * linesPerFile);
        }
        input = new MultipleFileInputStream(files);
        return input;
    }
}
