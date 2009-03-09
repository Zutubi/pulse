package com.zutubi.tove.type.record.events;

import com.zutubi.events.Event;
import com.zutubi.tove.type.record.RecordManager;

/**
 * Base for all record events.
 */
public abstract class RecordEvent extends Event
{
    protected String path;

    /**
     * Create a new record event.
     *
     * @param source the source that is raising the event
     * @param path   path of the affected record
     */
    public RecordEvent(RecordManager source, String path)
    {
        super(source);
        this.path = path;
    }

    public String getPath()
    {
        return path;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        RecordEvent that = (RecordEvent) o;

        if (!path.equals(that.path))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return path.hashCode();
    }
}
