/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.core.test;

import com.zutubi.util.Condition;
import com.zutubi.util.StringUtils;
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

    /**
     * Wait for the condition to be true before returning.  If the condition does not return true with
     * the given timeout, a runtime exception is generated with a message based on the description.  Note
     * that the wait will last at least as long as the timeout period, and maybe a little longer.
     *
     * @param condition     the condition which needs to be satisfied before returning
     * @param timeout       the amount of time given for the condition to return true before
     * generating a runtime exception
     * @param description   a human readable description of what the condition is waiting for which will be
     * used in the message of the generated timeout exception
     *
     * @throws RuntimeException if the timeout is reached or if this thread is interrupted.
     */
    public static void waitForCondition(Condition condition, long timeout, String description)
    {
        waitForCondition(condition, timeout, 200, description);
    }

    public static void waitForCondition(Condition condition, long timeout, long checkFrequency, String description)
    {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeout;
        
        if (endTime < startTime)
        {
            // we have wrapped around.
            endTime = Long.MAX_VALUE;
        }
        
        while(!condition.satisfied())
        {
            if(System.currentTimeMillis() > endTime)
            {
                throw new TimeoutException("Timed out waiting for " + description);
            }
            try
            {
                Thread.sleep(checkFrequency);
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException("Thread was interrupted.", e);
            }
        }
    }

    public static File getPulseRoot()
    {
        // Allow the root to be explicitly specified using a system property.
        String pulseRoot = System.getProperty(PROPERTY_PULSE_ROOT);
        if (StringUtils.stringSet(pulseRoot))
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
            File master = new File(candidate, MODULE_MASTER);
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
     * Waits for about 10 seconds for a TCP server to start listening on the
     * given port on this host.  A successful socket connection will be deemed
     * as indication that the server has started.
     *
     * @param port the port to wait for
     *
     * @throws InterruptedException if this thread is interrupted while
     *         sleeping
     * @throws RuntimeException if no server starts within about 10 seconds
     */
    public static void waitForServer(int port) throws InterruptedException
    {
        int retries = 0;

        while (true)
        {
            Socket sock = new Socket();
            try
            {
                sock.connect(new InetSocketAddress("localhost", port));
                break;
            }
            catch (IOException e)
            {
                if (retries++ < 100)
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
