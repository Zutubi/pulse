package com.zutubi.events;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Base class for all events.
 */
public class Event
{
    private static AtomicLong NEXT_ID = new AtomicLong(1);

    protected transient Object source;
    protected final long id;
    protected transient List<Exception> exceptions;

    /**
     * Create a new event.
     *
     * @param source the source that is raising the event
     */
    public Event(Object source)
    {
        this.source = source;
        this.id = NEXT_ID.getAndIncrement();
    }

    public Object getSource()
    {
        return source;
    }

    public void setSource(Object o)
    {
        source = o;
    }

    public long getId()
    {
        return id;
    }

    public void addException(Exception e)
    {
        if(exceptions == null)
        {
            exceptions = new LinkedList<Exception>();
        }

        exceptions.add(e);
    }

    public boolean hasExceptions()
    {
        return exceptions != null && exceptions.size() > 0;
    }
    
    public List<Exception> getExceptions()
    {
        return exceptions;
    }
}
