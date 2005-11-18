package com.cinnamonbob.test;

import junit.framework.TestCase;
import junit.framework.AssertionFailedError;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.io.*;
import java.net.URL;

/**
 * Base class for test cases.
 */
public abstract class BobTestCase extends TestCase
{
    public BobTestCase()
    {

    }

    public BobTestCase(String name)
    {
        super(name);
    }

    protected static void assertObjectEquals(Object a, Object b)
    {
        assertObjectEquals(null, a, b);
    }

    protected static void assertObjectEquals(String msg, Object a, Object b)
    {
        if (a instanceof Map)
        {
            assertEquals(msg, (Map) a, (Map) b);
        }
        else if (a instanceof List)
        {
            assertEquals(msg, (List) a, (List) b);
        }
        else if (a instanceof Collection)
        {
            assertEquals(msg, (Collection) a, (Collection) b);
        }
        else
        {
            assertEquals(msg, a, b);
        }
    }

    protected static void assertEquals(Map a, Map b)
    {
        assertEquals(null, a, b);
    }

    protected static void assertEquals(String msg, Map a, Map b)
    {
        if (msg == null)
            msg = "";
        assertEquals(msg + " [size difference]: ", a.size(), b.size());
        for (Object key : a.keySet())
        {
            assertObjectEquals(msg + " [property '" + key.toString() + "' difference]: ", a.get(key), b.get(key));
        }
    }

    protected static void assertEquals(List a, List b)
    {
        assertEquals(null, a, b);
    }

    protected static void assertEquals(String msg, List a, List b)
    {
        assertEquals(msg, a.size(), b.size());
        for (int i = 0; i < a.size(); i++)
        {
            assertObjectEquals(msg, a.get(i), b.get(i));
        }
    }

    protected static void assertEquals(Collection a, Collection b)
    {
        assertEquals(null, a, b);
    }

    protected static void assertEquals(String msg, Collection a, Collection b)
    {
        assertEquals(msg, a.size(), b.size());
        for (Object aA : a)
        {
            assertTrue(msg, b.contains(aA));
        }
    }

    /**
     * Compares the content of the two given directories recursively,
     * asserting that they have identical contents.  That is, all the
     * contained files and directories are the same, as is the
     * contents of the files.
     *
     * @param dir1 the first directory in the comparison
     * @param dir2 the second directory in the comparison
     * @throws junit.framework.AssertionFailedError if the given directories
     *         differ
     */
    protected static void assertDirectoriesEqual(File dir1, File dir2) throws IOException
    {
        if(!dir1.isDirectory())
        {
            throw new AssertionFailedError("Directory '" + dir1.getAbsolutePath() + "' does not exist or is not a directory");
        }

        if(!dir2.isDirectory())
        {
            throw new AssertionFailedError("Directory '" + dir2.getAbsolutePath() + "' does not exist or is not a directory");
        }

        String[] files1 = dir1.list();
        String[] files2 = dir2.list();

        // File.list does not guarantee ordering, so we do
        Arrays.sort(files1);
        Arrays.sort(files2);

        if(!Arrays.equals(files1, files2))
        {
            throw new AssertionFailedError("Directory contents differ: " +
                                           dir1.getAbsolutePath() + " = " + Arrays.toString(files1) + ", " +
                                           dir2.getAbsolutePath() + " = " + Arrays.toString(files2));
        }

        for(String file: files1)
        {
            File file1 = new File(dir1, file);
            File file2 = new File(dir2, file);

            if(file1.isDirectory())
            {
                assertDirectoriesEqual(file1, file2);
            }
            else
            {
                assertFilesEqual(file1, file2);
            }
        }
    }

    /**
     * Asserts that the contents of the two given files is identical.
     *
     * @param file1 the first file to compare
     * @param file2 the second file to compare
     * @throws AssertionFailedError if the contents of the files differ
     */
    protected static void assertFilesEqual(File file1, File file2) throws IOException
    {
        if(!file1.isFile())
        {
            throw new AssertionFailedError("File '" + file1.getAbsolutePath() + "' does not exist or is not a regular file");
        }

        if(!file2.isFile())
        {
            throw new AssertionFailedError("File '" + file2.getAbsolutePath() + "' does not exist or is not a regular file");
        }

        FileInputStream is1 = new FileInputStream(file1);
        FileInputStream is2 = new FileInputStream(file2);

        BufferedReader rs1 = new BufferedReader(new InputStreamReader(is1));
        BufferedReader rs2 = new BufferedReader(new InputStreamReader(is2));

        while (true)
        {
            String line1 = rs1.readLine();
            String line2 = rs2.readLine();

            if (line1 == null)
            {
                if (line2 == null)
                {
                    return;
                }
                throw new AssertionFailedError("Contents of '" + file1.getAbsolutePath() + " differs from contents of '" + file2.getAbsolutePath() + "'");
            }
            else
            {
                if (line2 == null)
                {
                    throw new AssertionFailedError("Contents of '" + file1.getAbsolutePath() + " differs from contents of '" + file2.getAbsolutePath() + "'");
                }
                assertEquals(line1, line2);
            }
        }
    }


    protected InputStream getInput(String testName)
    {
        return getClass().getResourceAsStream(getClass().getSimpleName() + "." + testName + ".xml");
    }

    protected URL getInputURL(String testName)
    {
        return getClass().getResource(getClass().getSimpleName() + "." + testName + ".xml");
    }
}
