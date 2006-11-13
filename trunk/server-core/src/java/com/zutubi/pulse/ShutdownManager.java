package com.zutubi.pulse;

import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.bootstrap.ComponentContext;

import java.util.List;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Manages orderly shutdown of the system.  The order is determined by a list
 * configured externally (e.g. via Spring).
 */
public class ShutdownManager
{
    /**
     * The list of objects that are called during a shutdown.  
     */
    private List<Stoppable> stoppables;
    private static final int EXIT_REBOOT = 111;

    /**
     * Set to true when the shutdown manager is executing a shutdown, false
     * otherwise.
     */
    private boolean shuttingDown = false;

    /**
     * Performs the shutdown sequence.
     *
     * @param force if true, forcibly terminate active tasks
     */
    public void shutdown(boolean force, boolean exitJvm)
    {
        // Ensure that we only handle one shutdown request at a time.
        synchronized(this)
        {
            if (shuttingDown)
            {
                return;
            }
            shuttingDown = true;
        }
        try
        {
            doShutdown(force, exitJvm);
        }
        finally
        {
            synchronized(this)
            {
                shuttingDown = false;
            }
        }
    }

    private void doShutdown(boolean force, boolean exitJvm)
    {
        if(checkWrapper(0))
        {
            return;
        }

        stop(force);

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

    public void stop(boolean force)
    {
        for (Stoppable stoppable : stoppables)
        {
            stoppable.stop(force);
        }
    }

    public boolean checkWrapper(int exitCode)
    {
        try
        {
            // If we are running under the Java Service Wrapper, stop via it.
            Class wrapperManagerClass = Class.forName("org.tanukisoftware.wrapper.WrapperManager");

            try
            {
                Method stopMethod = wrapperManagerClass.getMethod("stop", int.class);
                stopMethod.invoke(null, exitCode);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw e;
            }
        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }

    public void delayedShutdown(boolean force, boolean exitJvm)
    {
        ShutdownRunner runner = new ShutdownRunner(force, exitJvm);
        new Thread(runner).start();
    }

    public void delayedStop()
    {
        StopRunner runner = new StopRunner();
        new Thread(runner).start();
    }

    public void setStoppables(List<Stoppable> stoppables)
    {
        this.stoppables = stoppables;
    }

    public void reboot()
    {
        if(!checkWrapper(EXIT_REBOOT))
        {
            stop(true);
            System.exit(EXIT_REBOOT);
        }
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

    private class StopRunner implements Runnable
    {
        public void run()
        {
            try
            {
                Thread.sleep(500);
            }
            catch(InterruptedException e)
            {
                // Nothing
            }

            stop(true);
        }
    }
}
