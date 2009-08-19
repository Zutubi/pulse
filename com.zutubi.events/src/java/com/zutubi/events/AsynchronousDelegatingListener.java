package com.zutubi.events;

import com.zutubi.util.logging.Logger;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.util.concurrent.*;
import java.util.List;
import java.util.LinkedList;

/**
 * The Asynchronous delegating listener, as the name suggests, is an event listener that
 * delegates the handling of the event to another event listener (the delegate) that is
 * executed on a separate thread.
 *
 * The use of this delegating listener ensures that events do not block the event dispatch
 * thread and are delegated in the same order as they are received.
 *
 */
public class AsynchronousDelegatingListener implements EventListener
{
    private static final Logger LOG = Logger.getLogger(AsynchronousDelegatingListener.class);

    private final ExecutorService executor;
    private final EventListener delegate;

    /**
     * The futures allows us to track the processing of the events on the delegate thread,
     * allowing us to answer the question: Are you busy?
     */
    private final List<Future> futures = new LinkedList<Future>();

    public AsynchronousDelegatingListener(EventListener delegate, ThreadFactory threadFactory)
    {
        this.delegate = delegate;
        executor = Executors.newSingleThreadExecutor(threadFactory);
    }

    public void handleEvent(final Event event)
    {
        // deletate the handling of the event to the delegate being executed on
        // a separate thread. The executor will queue the event handling until the
        // thread is available. See Executors.newSingleThreadExecutor for full
        // sematic definition.
        if (!executor.isShutdown())
        {
            synchronized (futures)
            {
                // Cleanup any futures that are already done.  We do not need to track these any longer.
                cleanupCompletedEvents();

                futures.add(executor.submit(new Runnable()
                {
                    public void run()
                    {
                        delegate.handleEvent(event);
                    }
                }));
            }
        }
    }

    private void cleanupCompletedEvents()
    {
        List<Future> doneFutures = CollectionUtils.filter(futures, new Predicate<Future>()
        {
            public boolean satisfied(Future future)
            {
                return future.isDone();
            }
        });
        futures.removeAll(doneFutures);
    }

    public Class[] getHandledEvents()
    {
        return delegate.getHandledEvents();
    }

    public void stop(boolean force)
    {
        if (force)
        {
            executor.shutdownNow();
        }
        else
        {
            executor.shutdown();
            try
            {
                if (!executor.awaitTermination(600, TimeUnit.SECONDS))
                {
                    LOG.warning("Time out awaiting asynchronous listener termination");
                }
            }
            catch (InterruptedException e)
            {
                LOG.warning(e);
            }
        }
    }

    /**
     * Returns true if the asynchronous delegate is currently not processing any events and
     * there are no events queued to be processed.
     *
     * @return true if the delegate is idle, false otherwise.
     *
     * @see #isActive() 
     */
    public boolean isIdle()
    {
        synchronized (futures)
        {
            return CollectionUtils.find(futures, new Predicate<Future>()
            {
                public boolean satisfied(Future future)
                {
                    return !future.isDone();
                }
            }) == null;
        }
    }

    /**
     * Returns true if the asynchronous delegate is actively processing an event or if it has
     * a queued event that will be processed.
     *
     * @return true if the delegate is active, false otherwise.
     *
     * @see #isIdle()
     */
    public boolean isActive()
    {
        return !isIdle();
    }
}
