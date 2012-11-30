package com.zutubi.events;

import java.util.List;

/**
 * An event dispatcher implementation that uses the thread on which the event was published to
 * dispatch to the listeners.
 */
public class SynchronousDispatcher extends EventDispatcherSupport
{
    public void dispatch(Event event, List<EventListener> listeners)
    {
        safeDispatch(event, listeners);
    }
}
