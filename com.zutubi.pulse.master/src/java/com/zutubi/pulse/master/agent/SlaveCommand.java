package com.zutubi.pulse.master.agent;

import com.zutubi.pulse.core.engine.api.BuildException;

import java.io.OutputStream;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Holds details about a command being executed on an agent.  This is a simple executable, not a
 * command in the recipe sense, although perhaps it would be better if this mechanism was folded
 * into recipes somehow to share infrastructure.
 */
public class SlaveCommand
{
    private long id;
    private OutputStream stream;
    private Semaphore semaphore;
    private String failureMessage;

    public SlaveCommand(long id, OutputStream stream)
    {
        this.id = id;
        this.stream = stream;
        semaphore = new Semaphore(0);
    }

    public long getId()
    {
        return id;
    }

    public OutputStream getStream()
    {
        return stream;
    }

    public boolean waitFor(long timeout, TimeUnit timeUnit) throws InterruptedException
    {
        if (semaphore.tryAcquire(timeout, timeUnit))
        {
            if (failureMessage != null)
            {
                throw new BuildException(failureMessage);
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    public void success()
    {
        semaphore.release();
    }

    public void failure(String message)
    {
        this.failureMessage = message;
        semaphore.release();
    }
}
