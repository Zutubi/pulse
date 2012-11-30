package com.zutubi.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A listener that dispatches events to multiple delegates.  Each delegate must handle the same
 * types of events.  The implementation is thread safe - new delegates may be added or removed from
 * any thread at any time.  Changes to the wrapped delegates will be reflected in later event
 * handling (events already being handled will be unaffected).
 */
public class DemultiplexingListener implements EventListener
{
    private List<EventListener> delegates = new LinkedList<EventListener>();
    private Lock lock = new ReentrantLock();
    private Class[] handledEvents;

    /**
     * Creates a new listener that will listen for the given event types.
     *
     * @param handledEvents types of events to handle (and forward)
     */
    public DemultiplexingListener(Class... handledEvents)
    {
        this.handledEvents = handledEvents;
    }

    /**
     * Returns the wrapped listeners.  Changes to returned list have no effect on this listener.
     *
     * @return a snapshot of all listeners wrapped by this
     */
    public List<EventListener> getDelegates()
    {
        lock.lock();
        try
        {
            return new ArrayList<EventListener>(delegates);
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Adds a new delegate listener.  The listener must handle exactly the same types of events as
     * this.
     *
     * @param listener the listener to add
     * @throws IllegalArgumentException if the given listener does not handle the same event types
     */
    public void addDelegate(EventListener listener)
    {
        Class[] delegateEvents = listener.getHandledEvents();
        if (!Arrays.equals(handledEvents, delegateEvents))
        {
            throw new IllegalArgumentException("Listener does not handle the same events as this demultiplexer");
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

    /**
     * Removes a delegate listener if it is currently registered.
     *
     * @param listener the listener to remove
     * @return true if the listener was found and removed, false if it was not found
     */
    public boolean removeDelegate(EventListener listener)
    {
        lock.lock();
        try
        {
            return delegates.remove(listener);
        }
        finally
        {
            lock.unlock();
        }
    }

    public void handleEvent(Event evt)
    {
        List<EventListener> listeners = getDelegates();
        for(EventListener delegate: listeners)
        {
            delegate.handleEvent(evt);
        }
    }

    public Class[] getHandledEvents()
    {
        return handledEvents;
    }
}
