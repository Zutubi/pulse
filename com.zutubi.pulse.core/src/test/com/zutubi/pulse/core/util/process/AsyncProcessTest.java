package com.zutubi.pulse.core.util.process;

import static com.zutubi.pulse.core.test.EqualityAssertions.assertListEquals;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AsyncProcessTest extends PulseTestCase
{
    private File tempDir;

    protected void setUp() throws Exception
    {
        tempDir = FileSystemUtils.createTempDir(AsyncProcessTest.class.getName(), "");
        unzipInput("scripts", tempDir);
    }

    protected void tearDown() throws Exception
    {
        FileSystemUtils.rmdir(tempDir);
    }

    public void testSimpleLineBased() throws IOException, InterruptedException
    {
        Process p = createProcess("simple");
        if(p != null)
        {
            BufferingLineHandler lineHandler = new BufferingLineHandler();
            AsyncProcess ap = new AsyncProcess(p, lineHandler, true);
            ap.waitForSuccess();
            assertListEquals(lineHandler.getStdout(), "line 1", "line 2", "line 3");
            assertListEquals(lineHandler.getStderr());
        }
    }

    public void testStderrLineBased() throws IOException, InterruptedException
    {
        Process p = createProcess("stderr");
        if(p != null)
        {
            BufferingLineHandler lineHandler = new BufferingLineHandler();
            AsyncProcess ap = new AsyncProcess(p, lineHandler, true);
            ap.waitForSuccess();
            assertListEquals(lineHandler.getStdout());
            assertListEquals(lineHandler.getStderr(), "line 1", "line 2", "line 3");
        }
    }

    public void testForever() throws Exception
    {
        Process p = createProcess("forever");
        if(p != null)
        {
            final boolean[] called = new boolean[] { false };

            AsyncProcess ap = new AsyncProcess(p, new ByteHandler()
            {
                public void handle(byte[] buffer, int n, boolean error) throws Exception
                {
                    called[0] = true;
                }
            }, true);

            Integer exit = ap.waitFor(2, TimeUnit.SECONDS);
            assertNull(exit);
            assertTrue(called[0]);
            ap.destroy();
            joinThread(ap.stderrThread);
            joinThread(ap.stdoutThread);
            joinThread(ap.waiterThread);
        }
    }

    public void testLots() throws Exception
    {
        Process p = createProcess("lots");
        if(p != null)
        {
            BufferingLineHandler lineHandler = new BufferingLineHandler();
            AsyncProcess ap = new AsyncProcess(p, lineHandler, true);
            ap.waitForSuccess();
            assertListEquals(lineHandler.getStdout(), genArray(1000));
        }
    }

    public void testLotsToBothStreams() throws Exception
    {
        Process p = createProcess("lotstoboth");
        if(p != null)
        {
            BufferingLineHandler lineHandler = new BufferingLineHandler();
            AsyncProcess ap = new AsyncProcess(p, lineHandler, true);
            ap.waitForSuccess();
            String[] expectedOutput = multiplyArray(genArray(1000), 4);
            assertListEquals(lineHandler.getStdout(), expectedOutput);
            assertListEquals(lineHandler.getStderr(), expectedOutput);
        }
    }

    private String[] multiplyArray(String[] a, int count)
    {
        String[] result = new String[a.length * count];
        for(int i = 0; i < count; i++)
        {
            System.arraycopy(a, 0, result, i * a.length, a.length);
        }

        return result;
    }

    private String[] genArray(int count)
    {
        String[] result = new String[count];
        for(int i = 0; i < count; i++)
        {
            result[i] = "longline longline longline longline longline longline longline longline longline longline longline longline " + i;
        }
        return result;
    }

    private void joinThread(Thread thread) throws InterruptedException
    {
        thread.join(2000);
        assertFalse(thread.isAlive());
    }

    private Process createProcess(String name) throws IOException
    {
        if(SystemUtils.IS_WINDOWS)
        {
            File script = new File(tempDir, name + ".bat");
            return Runtime.getRuntime().exec(script.getAbsolutePath());
        }
        else if(SystemUtils.findInPath("bash") != null)
        {
            File script = new File(tempDir, name + ".sh");
            return Runtime.getRuntime().exec(new String[]{"bash", script.getAbsolutePath()});
        }

        return null;
    }
}
