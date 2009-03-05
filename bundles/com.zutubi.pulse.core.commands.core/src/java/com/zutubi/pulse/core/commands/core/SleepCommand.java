package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.commands.api.CommandSupport;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * A command that sleeps for a certain number of milliseconds.
 */
public class SleepCommand extends CommandSupport
{
    private Semaphore terminatedSemaphore = new Semaphore(0);

    public SleepCommand(SleepCommandConfiguration config)
    {
        super(config);
    }

    @Override
    public SleepCommandConfiguration getConfig()
    {
        return (SleepCommandConfiguration) super.getConfig();
    }

    public void execute(CommandContext commandContext)
    {
        try
        {
            if (terminatedSemaphore.tryAcquire(getConfig().getInterval(), TimeUnit.MILLISECONDS))
            {
                commandContext.error("Terminated");
            }
        }
        catch (InterruptedException e)
        {
            // Empty
        }
    }

    public void terminate()
    {
        terminatedSemaphore.release();
    }
}
