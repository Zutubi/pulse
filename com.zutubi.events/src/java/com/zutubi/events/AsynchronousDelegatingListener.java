package com.zutubi.events;

import com.zutubi.util.logging.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

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
/*
        if (!executor.isShutdown())
        {
*/
            executor.execute(new Runnable()
            {
                public void run()
                {
                    delegate.handleEvent(event);
                }
            });
/*
        }
        else
        {
            System.out.println("blah.." + event);
        }
*/
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
}
