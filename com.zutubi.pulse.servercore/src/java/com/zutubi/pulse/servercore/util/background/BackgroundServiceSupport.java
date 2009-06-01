package com.zutubi.pulse.servercore.util.background;

import com.zutubi.pulse.core.Stoppable;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Support for implementing a generic service that runs tasks in the background
 * using a thread pool.  Essentially a fairly thin wrapper around an {@link java.util.concurrent.ExecutorService}
 * which adapts the service to how Pulse wants it.  This includes use of the
 * Pulse thread factory, friendlier thread naming, and support for {@link com.zutubi.pulse.core.Stoppable}.
 * <p/>
 * In the future I would like to add some reporting to these services that can
 * be seen in the Pulse UI.
 * <p/>
 * To implement a background service, subclass this implementation and use
 * the executor service for submitting callables.  Ensure that init is called
 * (possibly by Spring) when you want the service to start, and that the
 * service is registered with the shutdown manager.
 */
public class BackgroundServiceSupport implements Stoppable
{
    private ThreadPoolExecutor executorService;
    private AtomicInteger nextId = new AtomicInteger(1);
    private String serviceName;

    private ThreadFactory threadFactory;
    private int maxThreadPoolSize;

    /**
     * Creates a new service with the given descriptive name.
     *
     * @param serviceName name of the service, used to tag threads which are
     *                    created by it for easy identification
     */
    public BackgroundServiceSupport(String serviceName)
    {
        this(serviceName, Integer.MAX_VALUE);
    }

    public BackgroundServiceSupport(String serviceName, int maxThreadPoolSize)
    {
        this.serviceName = serviceName;
        this.maxThreadPoolSize = maxThreadPoolSize;
    }

    /**
     * Initialises the service, creating the executor that will be used to
     * process tasks.
     */
    public void init()
    {
        executorService = new ThreadPoolExecutor(
                0,
                maxThreadPoolSize,
                60L,
                TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),
                new ThreadFactory()
                {
                    public Thread newThread(Runnable r)
                    {
                        Thread thread = threadFactory.newThread(r);
                        thread.setName(serviceName + " Service Worker " + nextId.getAndIncrement());
                        return thread;
                    }
                });
    }

    /**
     * Provides access to subclasses to the executor service that should be
     * used for submission of background tasks.
     *
     * @return the executor to use for task processing
     */
    protected ExecutorService getExecutorService()
    {
        return executorService;
    }

    /**
     * Stop this background service, optionally interrupting existing tasks.
     *
     * @param force {@inheritDoc}
     */
    public void stop(boolean force)
    {
        executorService.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        if (force)
        {
            executorService.shutdownNow();
        }
        else
        {
            executorService.shutdown();
        }
    }

    public void setThreadFactory(ThreadFactory threadFactory)
    {
        this.threadFactory = threadFactory;
    }
}
