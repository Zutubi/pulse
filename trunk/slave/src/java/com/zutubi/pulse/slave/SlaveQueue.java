package com.zutubi.pulse.slave;

import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.util.logging.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A simple FIFO queue for ordering build requests that come in to the slave.
 * The slave is dumb about scheduling: all "smarts" are on the master which
 * has full knowledge of all agents and thus can make better decisions.
 */
public class SlaveQueue implements Runnable, Stoppable
{
    private static final Logger LOG = Logger.getLogger(SlaveQueue.class);

    private Executor executor = Executors.newSingleThreadExecutor();
    private List<Runnable> recipes = new LinkedList<Runnable>();
    private Lock recipesLock = new ReentrantLock();
    private Condition recipesCondition = recipesLock.newCondition();
    private ExecutorService executorService;
    private boolean running = false;
    private boolean stopping = false;

    public void start()
    {
        if (isRunning())
        {
            throw new IllegalStateException("The queue is already running.");
        }

        LOG.debug("start();");
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(this);
    }

    public void stop()
    {
        if (!isRunning())
        {
            throw new IllegalStateException("The queue is not running.");
        }

        if (isStopping())
        {
            throw new IllegalStateException("The queue is already stopping.");
        }

        LOG.debug("stop();");
        recipesLock.lock();
        try
        {
            stopping = true;
            recipesCondition.signal();
        }
        finally
        {
            recipesLock.unlock();
        }
    }

    public void stop(boolean force)
    {
        stop();
    }

    public void run()
    {
        running = true;

        while(!stopping)
        {
            recipesLock.lock();
            try
            {
                while(recipes.isEmpty() && !stopping)
                {
                    try
                    {
                        recipesCondition.await();
                    }
                    catch (InterruptedException e)
                    {
                        LOG.warning("Wait was interrupted", e);
                    }
                }

                if(stopping)
                {
                    break;
                }

                executor.execute(recipes.remove(0));
            }
            finally
            {
                recipesLock.unlock();
            }
        }
        executorService.shutdown();
        running = false;
        stopping = false;
    }

    public boolean isRunning()
    {
        return running;
    }

    public boolean isStopping()
    {
        return stopping;
    }

    public void enqueue(Runnable recipe)
    {
        recipesLock.lock();
        try
        {
            recipes.add(recipe);
            recipesCondition.signal();
        }
        finally
        {
            recipesLock.unlock();
        }
    }

    public boolean enqueueExclusive(Runnable recipe)
    {
        boolean queued = false;

        recipesLock.lock();
        try
        {
            if(recipes.isEmpty())
            {
                enqueue(recipe);
                queued = true;
            }
        }
        finally
        {
            recipesLock.unlock();
        }

        return queued;
    }

    public void setExecutor(Executor executor)
    {
        this.executor = executor;
    }

    public int size()
    {
        return recipes.size();
    }

}
