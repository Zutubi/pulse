package com.zutubi.pulse.test;

import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.util.IOUtils;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Base class for test cases.
 */
public abstract class PulseTestCase extends TestCase
{
    public PulseTestCase()
    {

    }

    public PulseTestCase(String name)
    {
        super(name);
    }

    protected static void assertObjectEquals(Object a, Object b)
    {
        assertObjectEquals(null, a, b);
    }

    protected static void assertObjectEquals(String msg, Object a, Object b)
    {
        if (a == null)
        {
            assertNull(b);
            return;
        }

        if (a instanceof Map)
        {
            assertEquals(msg, (Map<?, ?>) a, (Map<?, ?>) b);
        }
        else if (a instanceof List)
        {
            assertEquals(msg, (List) a, (List) b);
        }
        else if (a instanceof Collection)
        {
            assertEquals(msg, (Collection) a, (Collection) b);
        }
        else if (a.getClass().isArray())
        {
            Arrays.equals((Object[]) a, (Object[]) b);
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
        {
            msg = "";
        }
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
     * @throws junit.framework.AssertionFailedError
     *          if the given directories
     *          differ
     */
    protected static void assertDirectoriesEqual(File dir1, File dir2) throws IOException
    {
        if (!dir1.isDirectory())
        {
            throw new AssertionFailedError("Directory '" + dir1.getAbsolutePath() + "' does not exist or is not a directory");
        }

        if (!dir2.isDirectory())
        {
            throw new AssertionFailedError("Directory '" + dir2.getAbsolutePath() + "' does not exist or is not a directory");
        }

        String[] files1 = dir1.list();
        String[] files2 = dir2.list();

        // File.list does not guarantee ordering, so we do
        Arrays.sort(files1);
        Arrays.sort(files2);

        List<String> fileList1 = new LinkedList<String>(Arrays.asList(files1));
        List<String> fileList2 = new LinkedList<String>(Arrays.asList(files2));

        // Ignore .svn directories
        fileList1.remove(".svn");
        fileList2.remove(".svn");

        if (!fileList1.equals(fileList2))
        {
            throw new AssertionFailedError("Directory contents differ: " +
                    dir1.getAbsolutePath() + " = " + fileList1 + ", " +
                    dir2.getAbsolutePath() + " = " + fileList2);
        }

        for (String file : fileList1)
        {
            File file1 = new File(dir1, file);
            File file2 = new File(dir2, file);

            if (file1.isDirectory())
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
        if (!file1.isFile())
        {
            throw new AssertionFailedError("File '" + file1.getAbsolutePath() + "' does not exist or is not a regular file");
        }

        if (!file2.isFile())
        {
            throw new AssertionFailedError("File '" + file2.getAbsolutePath() + "' does not exist or is not a regular file");
        }

        BufferedReader rs1 = null;
        BufferedReader rs2 = null;
        try
        {
            rs1 = new BufferedReader(new InputStreamReader(new FileInputStream(file1)));
            rs2 = new BufferedReader(new InputStreamReader(new FileInputStream(file2)));
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
        finally
        {
            IOUtils.close(rs1);
            IOUtils.close(rs2);
        }
    }

    protected static void assertStreamsEqual(InputStream is1, InputStream is2) throws IOException
    {
        try
        {
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
                    throw new AssertionFailedError("Contents of stream 1 differ from contents of stream 2. ");
                }
                else
                {
                    if (line2 == null)
                    {
                        throw new AssertionFailedError("Contents of stream 1 differ from contents of stream 2. ");
                    }
                    assertEquals(line1, line2);
                }
            }
        }
        finally
        {
            // close the streams for convenience.
            IOUtils.close(is1);
            IOUtils.close(is2);
        }
    }

    public void assertMatches(String expression, String got)
    {
        Pattern p = Pattern.compile(expression);
        assertTrue("'" + got + "' does not match expression '" + expression + "'", p.matcher(got).matches());
    }

    protected InputStream getInput(String testName)
    {
        return getInput(testName, "xml");
    }

    protected InputStream getInput(String testName, String extension)
    {
        return getClass().getResourceAsStream(getClass().getSimpleName() + "." + testName + "." + extension);
    }

    protected URL getInputURL(String testName)
    {
        return getInputURL(testName, "xml");
    }

    protected URL getInputURL(String testName, String extension)
    {
        return getClass().getResource(getClass().getSimpleName() + "." + testName + "." + extension);
    }

    protected File getTestDataDir(String module, String name)
    {
        return new File(getPulseRoot(), FileSystemUtils.composeFilename(module, "src", "test", getClass().getPackage().getName().replace('.', File.separatorChar), name));
    }

    protected File getTestDataFile(String module, String testName, String extension)
    {
        String testPart = testName == null ? "" : "." + testName;
        return new File(getPulseRoot(), FileSystemUtils.composeFilename(module, "src", "test", getClass().getName().replace('.', File.separatorChar) + testPart + "." + extension));
    }

    public static File getPulseRoot()
    {
        return TestUtils.getPulseRoot();
    }

    protected void removeDirectory(File dir) throws IOException
    {
        if (!FileSystemUtils.rmdir(dir))
        {
            throw new IOException("Failed to remove " + dir);
        }
    }

    public static void assertEndsWith(String a, String b)
    {
        assertTrue("'" + b + "' does not end with '" + a + "'", b.endsWith(a));
    }

    protected void waitForServer(int port) throws IOException
    {
        int retries = 0;

        while (true)
        {
            Socket sock = new Socket();
            try
            {
                sock.connect(new InetSocketAddress(port));
                break;
            }
            catch (IOException e)
            {
                if (retries++ < 10)
                {
                    try
                    {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException e1)
                    {
                    }
                }
                else
                {
                    throw new RuntimeException("Server did not start");
                }
            }
        }
    }

    protected <T> void assertListEquals(List<T> got, T... expected)
    {
        assertEquals(expected.length, got.size());
        for (int i = 0; i < expected.length; i++)
        {
            assertEquals(expected[i], got.get(i));
        }
    }

    protected void executeOnSeparateThreadAndWait(final Runnable r)
    {
        executeOnSeparateThreadAndWait(r, -1);
    }

    protected void executeOnSeparateThreadAndWait(final Runnable r, long timeout)
    {
        try
        {
            final AssertionFailedError[] afe = new AssertionFailedError[1];
            Thread thread = new Thread(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        r.run();
                    }
                    catch (AssertionFailedError e)
                    {
                        afe[0] = e;
                    }
                }
            });
            thread.start();
            if (timeout == -1)
            {
                thread.join();
            }
            else
            {
                thread.join(timeout);
            }

            if (afe[0] != null)
            {
                throw afe[0];
            }
        }
        catch (InterruptedException e)
        {
            // noop.
        }
    }

    protected void executeOnSeparateThread(final Runnable r)
    {
        Thread thread = new Thread(new Runnable()
        {
            public void run()
            {
                r.run();
            }
        });
        thread.start();
    }
}
