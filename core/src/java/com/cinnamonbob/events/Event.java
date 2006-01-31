package com.cinnamonbob.events;

/**
 * <class-comment/>
 */
public class Event
{
    Object source;

    public Event(Object source)
    {
        this.source = source;
    }

    public Object getSource()
    {
        return source;
    }

    public void setSource(Object o)
    {
        source = o;
    }
}
