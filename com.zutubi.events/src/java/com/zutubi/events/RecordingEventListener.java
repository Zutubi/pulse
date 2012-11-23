package com.zutubi.events;

import java.util.LinkedList;
import java.util.List;

/**
 * A listener that stores up all the events it has received.  Quite useful for testing, if nothing else.
 */
public class RecordingEventListener implements EventListener
{
    private Class[] handledEvents;
    private final List<Event> events = new LinkedList<Event>();

    /**
     * Creates a listener that will record events of the given types.
     *
     * @param handledEvents types of events to record
     */
    public RecordingEventListener(Class... handledEvents)
    {
        this.handledEvents = handledEvents;
    }

    public void handleEvent(Event evt)
    {
        events.add(evt);
    }

    public Class[] getHandledEvents()
    {
        return handledEvents;
    }

    /**
     * Returns all recorded events, in the order they were handled.
     *
     * @return a list of all events recorded thus far
     */
    public List<Event> getEventsReceived()
    {
        return events;
    }

    /**
     * Returns all recorded events of the given type, including subtypes, in the order they were handled.  Any event
     * that is an instanceof the given class is included.
     *
     * @return a list of all events of the given type recorded thus far
     */
    public <T extends Event> List<T> getEventsReceived(final Class<T> type)
    {
        List<T> result = new LinkedList<T>();
        for (Event e : events)
        {
            if (type.isInstance(e))
            {
                result.add(type.cast(e));
            }
        }

        return result;
    }

    /**
     * Indicates how many events have been received.
     *
     * @return the total number of events received
     */
    public int getReceivedCount()
    {
        return getEventsReceived().size();
    }

    /**
     * Indicates how many events of the given type, including subtypes, have been received.
     *
     * @param type type of events to count, all events that are an instanceof this type are included
     * @return the number of events of the given type recorded so far
     */
    public int getReceivedCount(Class<? extends Event> type)
    {
        return getEventsReceived(type).size();
    }

    /**
     * Clears the list of handled events.
     */
    public void reset()
    {
        getEventsReceived().clear();
    }
}
