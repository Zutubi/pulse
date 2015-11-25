package com.zutubi.pulse.core.events;

import com.zutubi.events.Event;

/**
 * Base for events sent from slaves to the master about running commands (simple exes, not in
 * recipes).
 */
public abstract class SlaveCommandEvent extends Event
{
    private long commandId;

    public SlaveCommandEvent(Object source, long commandId)
    {
        super(source);
        this.commandId = commandId;
    }

    public long getCommandId()
    {
        return commandId;
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

        SlaveCommandEvent that = (SlaveCommandEvent) o;

        return commandId == that.commandId;

    }

    @Override
    public int hashCode()
    {
        return (int) (commandId ^ (commandId >>> 32));
    }
}
