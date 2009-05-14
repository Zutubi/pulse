package com.zutubi.pulse.servercore;

import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.util.logging.Logger;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Manages orderly shutdown of the system.  The order is determined by a list
 * configured externally (e.g. via Spring).
 */
public class ShutdownManager
{
    private static final Logger LOG = Logger.getLogger(ShutdownManager.class);

    /**
     * The list of callbacks that are notified during shutdown.
     */
    private final List<Stoppable> stoppables = new LinkedList<Stoppable>();

    private static final int EXIT_REBOOT = 111;

    /**
     * Set to true when the shutdown manager is executing a shutdown, false
     * otherwise.
     */
    private boolean shuttingDown = false;

    /**
     * Performs the shutdown sequence.
     *
     * @param force   if true, forcibly terminate active tasks
     * @param exitJvm if true, explicitly exit the JVM
     */
    public void shutdown(boolean force, boolean exitJvm)
    {
        LOG.info("Shutdown requested.");
        System.err.printf("[%s] Shutting down...\n", getDateString());

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
            // why exit? because some external packages do not clean up all of their threads...
            LOG.info("System exiting with exit status 0");
            System.err.printf("[%s] Exiting.\n", getDateString());
            System.exit(0);
        }
        else
        {
            // cleanout the component context.
            SpringComponentContext.closeAll();
            System.err.printf("[%s] Shutdown complete.\n", getDateString());
        }
    }

    private String getDateString()
    {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG).format(new Date());
    }

    public void stop(boolean force)
    {
        List<Stoppable> stoppables = new LinkedList<Stoppable>();
        synchronized (this.stoppables)
        {
            stoppables.addAll(this.stoppables);
        }
        
        for (Stoppable stoppable : stoppables)
        {
            try
            {
                stoppable.stop(force);
            }
            catch (Exception e)
            {
                LOG.warning("Stoppable failed, cause: " + e.getMessage(), e);
            }
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
                LOG.severe(e);
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
        LOG.info("Delayed shutdown requested.");
        ShutdownRunner runner = new ShutdownRunner(force, exitJvm);
        new Thread(runner).start();
    }

    public void delayedStop()
    {
        LOG.info("Delayed stop requested.");
        StopRunner runner = new StopRunner();
        new Thread(runner).start();
    }

    public void setStoppables(List<Stoppable> stoppables)
    {
        if (stoppables == null)
        {
            stoppables = new LinkedList<Stoppable>();
        }

        for (Stoppable stoppable : stoppables)
        {
            if (stoppable == null)
            {
                throw new IllegalArgumentException("Stoppable instance can not be null.");
            }
        }

        synchronized (this.stoppables)
        {
            this.stoppables.clear();
            this.stoppables.addAll(stoppables);
        }
    }

    public void addStoppable(Stoppable stoppable)
    {
        if (stoppable == null)
        {
            throw new IllegalArgumentException("Stoppable instance can not be null.");
        }
        synchronized (stoppables)
        {
            stoppables.add(stoppable);
        }
    }

    public void reboot()
    {
        LOG.info("Reboot requested.");
        if(!checkWrapper(EXIT_REBOOT))
        {
            stop(true);
            LOG.info("System exiting with exit status " + EXIT_REBOOT);
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
            System.exit(0);
        }
    }
}
