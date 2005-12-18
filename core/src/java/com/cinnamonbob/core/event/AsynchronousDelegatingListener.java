package com.cinnamonbob.core.event;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 */
public class AsynchronousDelegatingListener implements EventListener
{
    private final ExecutorService executor;
    private final EventListener delegate;

    public AsynchronousDelegatingListener(EventListener delegate)
    {
        this.delegate = delegate;
        executor = Executors.newSingleThreadExecutor();
    }

    public void handleEvent(final Event event)
    {
        executor.execute(new Runnable()
        {
            public void run()
            {
                delegate.handleEvent(event);
            }
        });
    }

    public Class[] getHandledEvents()
    {
        return delegate.getHandledEvents();
    }

    public void stop()
    {
        executor.shutdownNow();
    }
}
