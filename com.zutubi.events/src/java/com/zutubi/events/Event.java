package com.zutubi.events;

import java.util.Collections;
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

    /**
     * Indicates who raised this event.
     *
     * @return the source of this event
     */
    public Object getSource()
    {
        return source;
    }

    /**
     * Returns a unique id for this event.  Within the same JVM process, all events have unique ids.
     *
     * @return a unique id for this event
     */
    public long getId()
    {
        return id;
    }

    /**
     * Records an exception against this event.  This is primarily used to record an exception while the event is
     * handled, which may be of interest to the event publisher.
     *
     * @param e the exception to record
     */
    public void addException(Exception e)
    {
        if (exceptions == null)
        {
            exceptions = new LinkedList<Exception>();
        }

        exceptions.add(e);
    }

    /**
     * Indicates if any exceptions have been recorded against this event.
     *
     * @return true if at least one exception has been recorded against this event
     */
    public boolean hasExceptions()
    {
        return exceptions != null && exceptions.size() > 0;
    }

    /**
     * Returns the exceptions recorded against this event, if any.
     *
     * @return an unmodifiable list of exceptions recorded against this event
     */
    public List<Exception> getExceptions()
    {
        return exceptions == null ? Collections.<Exception>emptyList() : Collections.unmodifiableList(exceptions);
    }
}
