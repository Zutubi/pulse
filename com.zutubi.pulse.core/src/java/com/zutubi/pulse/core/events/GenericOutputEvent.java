package com.zutubi.pulse.core.events;

import com.zutubi.events.Event;

import java.util.Arrays;

/**
 * Generic output event for sending a byte stream via the event system.
 */
public class GenericOutputEvent extends Event implements OutputEvent
{
    private long streamId;
    private byte[] data;

    public GenericOutputEvent(Object source, long streamId, byte[] data)
    {
        super(source);
        this.streamId = streamId;
        this.data = data;
    }

    public long getStreamId()
    {
        return streamId;
    }

    public byte[] getData()
    {
        return data;
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

        GenericOutputEvent that = (GenericOutputEvent) o;

        if (streamId != that.streamId)
        {
            return false;
        }
        if (!Arrays.equals(data, that.data))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = (int) (streamId ^ (streamId >>> 32));
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    public String toString()
    {
        return "Output Event: " + streamId;
    }
}
