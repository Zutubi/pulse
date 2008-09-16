package com.zutubi.pulse.events;

import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventListener;

/**
 * The base interface implemented by event managers.
 * 
 */
public interface EventManager
{
    /**
     * Register an event listener with the event manager so that it is notified
     * on the occurance of specific events.
     *
     * @param eventListener instance
     */
    void register(EventListener eventListener);

    /**
     * Unregister an event listener so that it is no longer notified of the occurance
     * of events.
     *
     * @param eventListener instance
     */
    void unregister(EventListener eventListener);

    /**
     * Publish an event to the event manager which will notify the appropriate event
     * listeners.  This is how events are sent out to all of the registered event
     * listeners.
     *
     * @param evt being published.
     */
    void publish(Event evt);
}
