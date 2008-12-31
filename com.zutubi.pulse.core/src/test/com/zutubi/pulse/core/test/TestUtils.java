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

    /**
     * Creates a new thread to execture the giving runnable, and starts the
     * thread.
     *
     * @param r the runnable for the thread to execute
     * @return the new thread
     */
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

    /**
     * Runs the given runnable on a new thread and waits for the thread to
     * complete.  If a test assertion fails in the thread the exception is
     * propagated into the calling thread (so the calling test case will fail).
     * If the timeout is non-negative, this method will wait for at most that
     * timeout for the runnable to complete before giving up and returning.
     *
     * @param r       the runnable to execute
     * @param timeout timeout to wait for the runnable to complete in
     *                milliseconds -- non-positive means wait forever
     * @throws AssertionFailedError if such an assertion is detected in the
     *         launched thread
     */
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
            if (timeout >= 0)
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

    /**
     * Equivalent to executeOnSeparateThreadAndWait(r, -1): executes the
     * runnable on a new background thread and waits indefinitely for it to
     * complete.
     *
     * @see #executeOnSeparateThreadAndWait(Runnable, long)
     *
     * @param r the runnable to execute
     */
    public static void executeOnSeparateThreadAndWait(final Runnable r)
    {
        executeOnSeparateThreadAndWait(r, -1);
    }

    /**
     * Waits for about a second for a TCP server to start listening on the
     * given port on this host.  A successful socket connection will be deemed
     * as indication that the server has started.
     *
     * @param port the port to wait for
     *
     * @throws InterruptedException if this thread is interrupted while
     *         sleeping
     * @throws RuntimeException if no server starts within about 1 second
     */
    public static void waitForServer(int port) throws InterruptedException
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
