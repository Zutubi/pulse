package com.zutubi.events;

/**
 * The base interface implemented by all classes interested in events.
 */
public interface EventListener
{
    /**
     * The callback method through which the listener is notified of the occurrence of an event.
     *
     * @param event the event to handle
     */
    void handleEvent(Event event);

    /**
     * Indicates the events that this listener should be notified of.  All published events that are instances of one of
     * the returned classes (as defined by instanceof) will be passed to this listener.
     * <p/>
     * Returning an empty array or an array with a single element being the base {@link Event} class indicates that the
     * listener should be notified of all events.
     *
     * @return the types of events this listener should be notified of
     */
    Class[] getHandledEvents();
}
