package com.zutubi.util;

import com.zutubi.util.logging.Logger;

import java.util.concurrent.*;
import java.util.List;

/**
 * Utilities built on top of java.util.concurrent classes.
 */
public class ConcurrentUtils
{
    /**
     * Runs the given callable asynchronously with the given timeout.  If the
     * task completes in time its result is returned, otherwise the task will
     * be interrupted and the default value returned.
     *
     * @param callable     the task to run
     * @param timeout      the timeout magnitude
     * @param timeUnit     the timeout units
     * @param defaultValue the value to reutrn in the case of timeout
     * @param <T>          the return type of the task
     * @return the result of the task if it completes in time, otherwise
     *         defaultValue
     * @throws RuntimeException if the task throws an exception
     */
    public static <T> T runWithTimeout(Callable<T> callable, long timeout, TimeUnit timeUnit, T defaultValue)
    {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<T> future = executor.submit(callable);
        try
        {
            return future.get(timeout, timeUnit);
        }
        catch (InterruptedException e)
        {
            return defaultValue;
        }
        catch (ExecutionException e)
        {
            throw new RuntimeException(e);
        }
        catch (TimeoutException e)
        {
            future.cancel(true);
            return defaultValue;
        }
    }

    /**
     * Wait for the provided list of futures to complete before returning.
     *
     * @param futures   the futures we are waiting on
     *
     * @throws InterruptedException if the thread waiting for the futures is interrupted.
     */
    public static void waitForTasks(List<Future> futures) throws InterruptedException
    {
        waitForTasks(futures, null);        
    }

    /**
     * Wait for the provided list of futures to complete before returning.
     *
     * @param futures   the futures we are waiting on
     * @param log       the logger to received details of any exceptions encountered during
     * the execution of the tasks.
     *
     * @throws InterruptedException if the thread waiting for the futures is interrupted.
     */
    public static void waitForTasks(List<Future> futures, Logger log) throws InterruptedException
    {
        for (Future task : futures)
        {
            try
            {
                task.get();
            }
            catch (CancellationException e)
            {
                // the task was cancelled, and hence is complete.  Lets keep going.
            }
            catch (ExecutionException e)
            {
                // the task generated an exception during execution. Log it and continue.
                if (log != null)
                {
                    log.severe(e);
                }
            }
        }
    }
}
