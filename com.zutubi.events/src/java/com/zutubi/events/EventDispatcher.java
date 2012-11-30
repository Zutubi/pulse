package com.zutubi.events;

import java.util.List;

/**
 * Contract for implementations that hand events from the manager to listeners.
 */
public interface EventDispatcher
{
    /**
     * Dispatches the given event to the given listeners.  The list is already filtered to only
     * include listeners that are interested in the event.
     *
     * @param event the event to dispatch
     * @param listeners listeners to dispatch the event to
     */
    void dispatch(Event event, List<EventListener> listeners);
}
