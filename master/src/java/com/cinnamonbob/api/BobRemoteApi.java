package com.cinnamonbob.api;

import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.ShutdownManager;

/**
 * Implements a simple API for remote monitoring and control.
 */
public class BobRemoteApi
{
    public boolean shutdown(boolean force)
    {
        // Sigh ... this is tricky, because if we shutdown here Jetty dies
        // before this request is complete and the client gets an error :-|.
        ShutdownRunner runner = new ShutdownRunner(force);
        new Thread(runner).start();
        return true;
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
            }
            ShutdownManager shutdownManager = (ShutdownManager) ComponentContext.getBean("shutdownManager");
            shutdownManager.shutdown(force);
        }
    }
}
