package com.zutubi.pulse.core.util.process;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


public class ProcessControlTest extends PulseTestCase
{
    private File tempDir;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        tempDir = FileSystemUtils.createTempDir(getName(), "");
    }

    @Override
    protected void tearDown() throws Exception
    {
        removeDirectory(tempDir);
        super.tearDown();
    }

    public void testDestroyTree() throws Exception
    {
        if (ProcessControl.isNativeDestroyAvailable())
        {
            File buildFile = copyInputToDirectory("xml", tempDir);
            String ant = SystemUtils.IS_WINDOWS ? "ant.bat" : "ant";
            ProcessBuilder processBuilder = new ProcessBuilder(ant, "-f", buildFile.getAbsolutePath(), "doit");
            processBuilder.redirectErrorStream(true);
            final Process p = processBuilder.start();

            Thread killer = new Thread(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        Thread.sleep(2000);
                    }
                    catch (InterruptedException e)
                    {
                        // noop
                    }
                    ProcessControl.destroyProcess(p);
                }
            });
            killer.start();

            try
            {
                InputStream is = p.getInputStream();
                byte[] buffer = new byte[1024];
                while (is.read(buffer) > 0) {}
            }
            catch (IOException e)
            {
                // Can be raised when the process is killed.
            }

            p.waitFor();
            killer.join();
        }
    }
    
    public void testGetPid() throws IOException, InterruptedException
    {
        if (ProcessControl.isPidAvailable())
        {
            ProcessBuilder builder = new ProcessBuilder("sh");
            Process p = builder.start();
            assertTrue(ProcessControl.getPid(p) != 0);
            ProcessControl.destroyProcess(p);
            p.waitFor();
        }
    }
}
