package com.cinnamonbob.event;

import java.util.List;
import java.util.LinkedList;

/**
 * <class-comment/>
 */
public class DefaultEventManager implements EventManager
{
    protected final List<EventListener> listeners = new LinkedList<EventListener>();

    private EventDispatcher dispatcher;

    public DefaultEventManager()
    {
         this(new SynchronousDispatcher());
    }

    public DefaultEventManager(EventDispatcher dispatcher)
    {
        this.dispatcher = dispatcher;
    }

    public void register(EventListener listener)
    {
        if (!listeners.contains(listener))
        {
            listeners.add(listener);
        }
    }

    public void unregister(EventListener listener)
    {
        listeners.remove(listener);
    }

    public void publish(Event evt)
    {
        List<EventListener> copy = new LinkedList<EventListener>(listeners);
        dispatcher.dispatch(evt, copy);
    }
}