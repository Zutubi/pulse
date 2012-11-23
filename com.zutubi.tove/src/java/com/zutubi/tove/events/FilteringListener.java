package com.zutubi.tove.events;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.util.Predicate;

/**
 * A listener that filters events before passing them on to a delegate,
 * allowing selection of events based on more than just the event class.
 */
public class FilteringListener implements EventListener
{
    private Predicate<Event> predicate;
    private EventListener delegate;

    public FilteringListener(Predicate<Event> predicate, EventListener delegate)
    {
        this.predicate = predicate;
        this.delegate = delegate;
    }

    public void handleEvent(Event evt)
    {
        if(predicate.satisfied(evt))
        {
            delegate.handleEvent(evt);
        }
    }

    public Class[] getHandledEvents()
    {
        return delegate.getHandledEvents();
    }
}
