package com.zutubi.events;

import java.util.LinkedList;
import java.util.List;

/**
 * A listener that stores up all the events it has received.  Quite useful for
 * testing.
 */
public class RecordingEventListener implements EventListener
{
    private Class[] handledEvents;
    private final List<Event> events = new LinkedList<Event>();

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

    public List<Event> getEventsReceived()
    {
        return events;
    }

    public int getReceivedCount()
    {
        return getEventsReceived().size();
    }

    public void reset()
    {
        getEventsReceived().clear();
    }
}
