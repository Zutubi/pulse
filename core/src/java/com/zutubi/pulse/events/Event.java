package com.zutubi.pulse.events;

import java.util.concurrent.atomic.AtomicLong;

/**
 * <class-comment/>
 */
public class Event<T>
{
    private static AtomicLong NEXT_ID = new AtomicLong(1);

    protected transient T source;

    protected final long id;

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
}
