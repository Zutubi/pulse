/********************************************************************************
  @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.events;

/**
 * <class-comment/>
 */
public class Event<T>
{
    T source;

    public Event(T source)
    {
        this.source = source;
    }

    public T getSource()
    {
        return source;
    }

    public void setSource(T o)
    {
        source = o;
    }
}
