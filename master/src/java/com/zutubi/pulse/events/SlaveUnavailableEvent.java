/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.events;

import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.model.Slave;

/**
 */
public class SlaveUnavailableEvent extends SlaveEvent
{
    public SlaveUnavailableEvent(Object source, Slave slave)
    {
        super(source, slave);
    }
}
