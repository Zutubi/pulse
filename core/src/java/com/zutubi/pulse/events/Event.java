package com.zutubi.pulse.events;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <class-comment/>
 */
public class Event<T>
{
    private static AtomicLong NEXT_ID = new AtomicLong(1);

    protected transient T source;
    protected final long id;
    protected transient List<Exception> exceptions;

    public Event(T source)
    {
        this.source = source;
        this.id = NEXT_ID.getAndIncrement();
    }

    public T getSource()
    {
        return source;
    }

    public void setSource(T o)
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
