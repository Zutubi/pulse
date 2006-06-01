package com.zutubi.pulse;

import com.zutubi.pulse.core.Stoppable;

import java.util.List;

/**
 * Manages orderly shutdown of the system.  The order is determined by a list
 * configured externally (e.g. via Spring).
 */
public class ShutdownManager
{
    private List<Stoppable> stoppables;

    /**
     * Performs the shutdown sequence.
     *
     * @param force if true, forcibly terminate active tasks
     */
    public void shutdown(boolean force)
    {
        for (Stoppable stoppable : stoppables)
        {
            stoppable.stop(force);
        }
        System.exit(0);
    }

    public void delayedShutdown(boolean force)
    {
        ShutdownRunner runner = new ShutdownRunner(force);
        new Thread(runner).start();
    }

    public void setStoppables(List<Stoppable> stoppables)
    {
        this.stoppables = stoppables;
    }

    private class ShutdownRunner implements Runnable
    {
        private boolean force;

        public ShutdownRunner(boolean force)
        {
            this.force = force;
        }

        public void run()
        {
            // Oh my, is this ever dodgy...
            try
            {
                Thread.sleep(500);
            }
            catch (InterruptedException e)
            {
                // Empty
            }
            shutdown(force);
        }
    }
}
