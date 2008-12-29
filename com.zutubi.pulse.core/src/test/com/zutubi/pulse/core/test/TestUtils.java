package com.zutubi.pulse.core.test;

import com.zutubi.pulse.Version;
import com.zutubi.util.TextUtils;
import junit.framework.AssertionFailedError;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Various utilities for use by test cases.
 */
public class TestUtils
{
    public static File getPulseRoot()
    {
        // First, take a guess at the working directory (which is likely to
        // work if we are running tests using Ant)
        String pulseRoot = System.getProperty("pulse.root");
        if (TextUtils.stringSet(pulseRoot))
        {
            File rootFile = new File(pulseRoot);
            if (rootFile.isDirectory())
            {
                return rootFile;
            }
        }

        File master = new File("com.zutubi.pulse.master");
        if (master.isDirectory())
        {
            return master.getAbsoluteFile().getParentFile();
        }

        master = new File("../com.zutubi.pulse.master");
        if (master.isDirectory())
        {
            return master.getAbsoluteFile().getParentFile();
        }

        // OK, maybe we can find indirectly via the classpath
        URL resource = Version.class.getResource("version.properties");
        try
        {
            File resourceFile = new File(resource.toURI());
            return new File(resourceFile.getAbsolutePath().replaceFirst("com.zutubi.pulse.core/classes/.*", ""));
        }
        catch (URISyntaxException e)
        {
            // Not possible.
            e.printStackTrace();
            return null;
        }
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
