package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.RandomUtils;
import com.zutubi.util.io.IOUtils;

import java.io.*;

/**
 *
 *
 */
public class TailTest extends PulseTestCase
{
    private File tmp;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = FileSystemUtils.createTempDir();
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmp);

        super.tearDown();
    }

    public void testDefaultTail() throws IOException
    {
        File f = generateTestFile(100);
        Tail tail = new Tail();
        tail.setFile(f);

        String str = tail.getTail();
        assertEquals("91\n92\n93\n94\n95\n96\n97\n98\n99\n100\n", str);
    }

    public void testCustomLines() throws IOException
    {
        File f = generateTestFile(100);
        Tail tail = new Tail();
        tail.setFile(f);
        tail.setMaxLines(5);

        String str = tail.getTail();
        assertEquals("96\n97\n98\n99\n100\n", str);
    }

    public void testZeroLines() throws IOException
    {
        File f = generateTestFile(100);
        Tail tail = new Tail();
        tail.setFile(f);
        tail.setMaxLines(0);

        String str = tail.getTail();
        assertEquals("", str);
    }

    public void testMoreLinesThanInFile() throws IOException
    {
        File f = generateTestFile(5);
        Tail tail = new Tail();
        tail.setFile(f);
        tail.setMaxLines(10);

        String str = tail.getTail();
        assertEquals("1\n2\n3\n4\n5\n", str);
    }

    public void testZeroLengthFile() throws IOException
    {
        File f = generateTestFile(0);
        Tail tail = new Tail();
        tail.setFile(f);

        String str = tail.getTail();
        assertEquals("", str);
    }

    /**
     * Generate a new randomly named test file that contains the line number on each line.
     *
     * @param length in lines for this file.
     *
     * @return a randomly named file
     * 
     * @throws IOException is raised if we have problems writing to the file.
     */
    private File generateTestFile(long length) throws IOException
    {
        File file = createRandomFile();
        
        BufferedWriter out = null;
        try
        {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            for (long l = 0; l < length; l++)
            {
                out.write(Long.toString(l + 1));
                out.newLine();
            }
        }
        finally
        {
            IOUtils.close(out);
        }
        return file;
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
