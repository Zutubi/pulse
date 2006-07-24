package com.zutubi.pulse;

import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.bootstrap.ComponentContext;

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
    public void shutdown(boolean force, boolean exitJvm)
    {
        for (Stoppable stoppable : stoppables)
        {
            stoppable.stop(force);
        }
        if (exitJvm)
        {
            // why exit? because some external packages do not clean up all of there threads... 
            System.exit(0);
        }
        else
        {
            // cleanout the component context.
            ComponentContext.closeAll();
        }
    }

    public void delayedShutdown(boolean force, boolean exitJvm)
    {
        ShutdownRunner runner = new ShutdownRunner(force, exitJvm);
        new Thread(runner).start();
    }

    public void setStoppables(List<Stoppable> stoppables)
    {
        this.stoppables = stoppables;
    }

    private class ShutdownRunner implements Runnable
    {
        private boolean force;
        private boolean exitJvm;

        public ShutdownRunner(boolean force, boolean exitJvm)
        {
            this.force = force;
            this.exitJvm = exitJvm;
        }

        public void run()
        {
            // Oh my, is this ever dodgy...
            try
            {
                //TODO: work out a way to shutdown jetty where by it stops accepting inbound requests
                // and then shuts itself down after it has finished processing the final active request.
                Thread.sleep(500);
            }
            catch (InterruptedException e)
            {
                // Empty
            }
            shutdown(force, exitJvm);
        }
    }
}
