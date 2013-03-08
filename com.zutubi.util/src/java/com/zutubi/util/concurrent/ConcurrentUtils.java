package com.zutubi.util.concurrent;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.UncheckedTimeoutException;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
        try
        {
            return new SimpleTimeLimiter(Executors.newSingleThreadExecutor()).callWithTimeout(callable, timeout, timeUnit, true);
        }
        catch (InterruptedException e)
        {
            return defaultValue;
        }
        catch (UncheckedTimeoutException e)
        {
            return defaultValue;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
