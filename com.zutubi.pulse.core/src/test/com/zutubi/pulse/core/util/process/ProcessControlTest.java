package com.zutubi.pulse.core.util.process;

import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;


public class ProcessControlTest extends PulseTestCase
{
    private File tempDir;

    @Override
    protected void setUp() throws Exception
    {
        tempDir = FileSystemUtils.createTempDir(getName(), "");
    }

    @Override
    protected void tearDown() throws Exception
    {
        removeDirectory(tempDir);
    }

    public void testDestroyTree() throws Exception
    {
        System.setProperty(ProcessControl.NATIVE_PROCESS_KILL, "true");
        if(ProcessControl.init())
        {
            File buildFile = new File(tempDir, "build.xml");
            IOUtils.joinStreams(getInput("xml"), new FileOutputStream(buildFile), true);
            ProcessBuilder processBuilder = new ProcessBuilder("ant.bat", "-f", buildFile.getAbsolutePath(), "doit");
            processBuilder.redirectErrorStream(true);
            final Process p = processBuilder.start();

            Thread killer = new Thread(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        Thread.sleep(5000);
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
