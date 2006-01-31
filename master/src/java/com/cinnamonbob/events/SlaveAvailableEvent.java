package com.cinnamonbob.events;

import com.cinnamonbob.events.Event;
import com.cinnamonbob.model.Slave;

/**
 */
public class SlaveAvailableEvent extends Event
{
    private Slave slave;

    public SlaveAvailableEvent(Object source, Slave slave)
    {
        super(source);
        this.slave = slave;
    }

    public Slave getSlave()
    {
        return slave;
    }
}
