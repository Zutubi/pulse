package com.cinnamonbob.events;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <class-comment/>
 */
public class AsynchronousDispatcher implements EventDispatcher
{
    private final ExecutorService executor;

    public AsynchronousDispatcher()
    {
        executor = Executors.newSingleThreadExecutor();
    }

    public void dispatch(final Event evt, final List<EventListener> listeners)
    {
        executor.execute(new Runnable()
        {
            public void run()
            {
                for (EventListener listener: listeners)
                {
                    listener.handleEvent(evt);
                }
            }
        });
    }
}
