package com.zutubi.pulse.core.util.process;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.FileSystemUtils;

import java.io.File;
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
        System.setProperty(ProcessControl.NATIVE_PROCESS_KILL, "true");
        if(ProcessControl.init())
        {
            File buildFile = copyInputToDirectory("xml", tempDir);
            ProcessBuilder processBuilder = new ProcessBuilder("ant.bat", "-f", buildFile.getAbsolutePath(), "doit");
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

            InputStream is = p.getInputStream();
            byte[] buffer = new byte[1024];
            while (is.read(buffer) > 0);

            p.waitFor();
            killer.join();
        }
    }
}
