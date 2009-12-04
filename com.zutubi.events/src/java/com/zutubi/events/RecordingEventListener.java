package com.zutubi.events;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

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

    public <T extends Event> List<T> getEventsReceived(final Class<T> type)
    {
        return (List<T>) CollectionUtils.filter(events, new Predicate<Event>()
        {
            public boolean satisfied(Event event)
            {
                return event.getClass() == type;
            }
        });
    }

    public int getReceivedCount()
    {
        return getEventsReceived().size();
    }

    public int getReceivedCount(Class<? extends Event> type)
    {
        return getEventsReceived(type).size();
    }

    public void reset()
    {
        getEventsReceived().clear();
    }
}
