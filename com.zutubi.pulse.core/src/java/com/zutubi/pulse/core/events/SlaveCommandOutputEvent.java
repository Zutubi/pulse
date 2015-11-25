package com.zutubi.pulse.core.events;

import java.util.Arrays;

/**
 * Generic output event for sending a byte stream via the event system.
 */
public class SlaveCommandOutputEvent extends SlaveCommandEvent implements OutputEvent
{
    private byte[] data;

    public SlaveCommandOutputEvent(Object source, long commandId, byte[] data)
    {
        super(source, commandId);
        this.data = data;
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
        if (!super.equals(o))
        {
            return false;
        }

        SlaveCommandOutputEvent that = (SlaveCommandOutputEvent) o;

        return Arrays.equals(getData(), that.getData());

    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(getData());
        return result;
    }

    public String toString()
    {
        return "Output Event: " + getCommandId();
    }
}
