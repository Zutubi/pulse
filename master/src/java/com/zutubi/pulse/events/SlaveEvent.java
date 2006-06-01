package com.zutubi.pulse.events;

import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.model.Slave;

/**
 */
public class SlaveEvent extends Event
{
    private Slave slave;

    public SlaveEvent(Object source, Slave slave)
    {
        super(source);
        this.slave = slave;
    }

    public Slave getSlave()
    {
        return slave;
    }
}
