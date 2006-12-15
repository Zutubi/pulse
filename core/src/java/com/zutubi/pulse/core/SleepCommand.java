package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * A command that sleeps for a certain number of milliseconds.
 */
public class SleepCommand extends CommandSupport
{
    /**
     * Number of milliseconds to sleep.
     */
    private long interval;
    private Semaphore terminatedSemaphore = new Semaphore(0);

    public void execute(CommandContext context, CommandResult result)
    {
        try
        {
            if (terminatedSemaphore.tryAcquire(interval, TimeUnit.MILLISECONDS))
            {
                result.error("Terminated");
            }
        }
        catch (InterruptedException e)
        {
            // Empty
        }
    }

    /**
     * The sleep interval in milliseconds.
     *
     * @param interval value.
     */
    public void setInterval(long interval)
    {
        this.interval = interval;
    }

    public void terminate()
    {
        terminatedSemaphore.release();
    }
}
