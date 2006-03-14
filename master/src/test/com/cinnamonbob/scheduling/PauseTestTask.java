package com.cinnamonbob.scheduling;

/**
 * <class-comment/>
 */
public class PauseTestTask implements Task
{
    static final Object lock = new Object();

    public static void unpause()
    {
        synchronized(lock)
        {
            lock.notifyAll();
        }
    }

    public void execute(TaskExecutionContext context)
    {
        try
        {
            synchronized(lock)
            {
                lock.wait();
            }
        }
        catch (InterruptedException e)
        {
            // noop.
        }
    }
}
