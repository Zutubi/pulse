package com.zutubi.pulse.core.test;

import com.zutubi.util.TextUtils;
import junit.framework.AssertionFailedError;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Various utilities for use by test cases.
 */
public class TestUtils
{
    private static final String PROPERTY_PULSE_ROOT = "pulse.root";
    private static final String PROPERTY_WORKING_DIRECTORY = "user.dir";

    private static final String MODULE_MASTER = "com.zutubi.pulse.master";

    public static File getPulseRoot()
    {
        // Allow the root to be expllicitly specified using a system property.
        String pulseRoot = System.getProperty(PROPERTY_PULSE_ROOT);
        if (TextUtils.stringSet(pulseRoot))
        {
            File rootFile = new File(pulseRoot);
            if (rootFile.isDirectory())
            {
                return rootFile;
            }
        }

        // Look for the root starting at the working directory and moving
        // upwards in the hierarchy until we find it or hit the root.
        File candidate = new File(System.getProperty(PROPERTY_WORKING_DIRECTORY));
        while (candidate != null)
        {
            File master = new File(MODULE_MASTER);
            if (master.isDirectory())
            {
                return candidate.getAbsoluteFile();
            }

            candidate = candidate.getParentFile();
        }

        throw new RuntimeException("Unable to determine Pulse source root");
    }

    public static Thread executeOnSeparateThread(final Runnable r)
    {
        Thread thread = new Thread(new Runnable()
        {
            public void run()
            {
                r.run();
            }
        });
        thread.start();
        return thread;
    }

    public static void executeOnSeparateThreadAndWait(final Runnable r, long timeout)
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

    public static void executeOnSeparateThreadAndWait(final Runnable r)
    {
        executeOnSeparateThreadAndWait(r, -1);
    }

    public static void waitForServer(int port) throws IOException, InterruptedException
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
                    Thread.sleep(100);
                }
                else
                {
                    throw new RuntimeException("Server did not start");
                }
            }
        }
    }
}
