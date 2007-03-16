package com.zutubi.pulse.jni;

import com.zutubi.pulse.test.PulseTestCase;

import java.io.File;
import java.io.InputStream;


/**
 */
public class ProcessControlTest extends PulseTestCase
{
    public void testDestroyTree() throws Exception
    {
        System.setProperty(ProcessControl.NATIVE_PROCESS_KILL, "true");
        if(ProcessControl.init())
        {
            File buildFile = getTestDataFile("core", getName(), "xml");
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
