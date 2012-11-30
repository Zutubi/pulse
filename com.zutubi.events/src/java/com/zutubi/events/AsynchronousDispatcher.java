package com.zutubi.events;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An event dispatcher that dispatches events on a separate thread to the thread on which the event
 * was raised.  All dispatched events are processed on a single thread to maintain the order in
 * which they are handled.
 */
public class AsynchronousDispatcher extends EventDispatcherSupport
{
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void dispatch(final Event event, final List<EventListener> listeners)
    {
        executor.execute(new Runnable()
        {
            public void run()
            {
                safeDispatch(event, listeners);
            }
        });
    }
}
