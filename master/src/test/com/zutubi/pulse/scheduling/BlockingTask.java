/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.scheduling;

/**
 * <class-comment/>
 */
public class BlockingTask implements Task
{
    private static final Object lock = new Object();
    private static boolean waiting = false;

    public void execute(TaskExecutionContext context)
    {
        DefaultTriggerHandlerTest.stopWaiting();
        synchronized(lock)
        {
            waiting = true;
            while (waiting)
            {
                try
                {
                    lock.wait();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void stopWaiting()
    {
        synchronized(lock)
        {
            if (waiting)
            {
                waiting = false;
                lock.notifyAll();
            }
        }
    }
}
