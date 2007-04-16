package com.zutubi.pulse.events;

import com.zutubi.pulse.util.CollectionUtils;
import com.zutubi.pulse.util.Predicate;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A listener that dispatches events to multiple delegates.  Each delegate
 * must handle the same types of events.
 */
public class MultiplexingListener implements EventListener
{
    private List<EventListener> delegates = new LinkedList<EventListener>();
    private Lock lock = new ReentrantLock();
    private Class[] handledEvents;

    public MultiplexingListener(Class... handledEvents)
    {
        this.handledEvents = handledEvents;
    }

    public void addDelegate(EventListener listener)
    {
        Class[] delegateEvents = listener.getHandledEvents();
        if(!CollectionUtils.equals(handledEvents, delegateEvents))
        {
            throw new IllegalArgumentException("Listener does not handle the same events as this multiplexer");
        }

        lock.lock();
        try
        {
            delegates.add(listener);
        }
        finally
        {
            lock.unlock();
        }
    }

    public void removeDelegate(Predicate<EventListener> predicate)
    {
        lock.lock();
        try
        {
            Iterator<EventListener> it = delegates.iterator();
            while(it.hasNext())
            {
                if(predicate.satisfied(it.next()))
                {
                    it.remove();
                }
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public void handleEvent(Event evt)
    {
        lock.lock();
        try
        {
            for(EventListener delegate: delegates)
            {
                delegate.handleEvent(evt);
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public Class[] getHandledEvents()
    {
        return handledEvents;
    }
}
