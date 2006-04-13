/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.events;

import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.util.logging.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 */
public class AsynchronousDelegatingListener implements EventListener, Stoppable
{
    private static final Logger LOG = Logger.getLogger(AsynchronousDelegatingListener.class);

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
