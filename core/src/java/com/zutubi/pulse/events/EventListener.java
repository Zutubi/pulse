package com.zutubi.pulse.events;

/**
 * The base interface implemented by all classes interested in events.
 *
 */
public interface EventListener extends java.util.EventListener
{
    /**
     * The callback method throw which the listener is notified of the
     * occurance of an event.
     *
     * @param event in question
     */
    void handleEvent(Event event);

    /**
     * Return the list of classes defining the events that this listener
     * is interested in.
     *
     * Returning the base event class will result in being notified of all
     * events.
     *
     * @return class array.
     */
    Class[] getHandledEvents();
}
