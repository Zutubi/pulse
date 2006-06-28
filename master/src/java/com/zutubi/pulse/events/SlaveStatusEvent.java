package com.zutubi.pulse.events;

import com.zutubi.pulse.agent.SlaveAgent;
import com.zutubi.pulse.agent.Status;

/**
 */
public class SlaveStatusEvent extends SlaveEvent
{
    private Status oldStatus;

    public SlaveStatusEvent(Object source, Status oldStatus, SlaveAgent agent)
    {
        super(source, agent);
        this.oldStatus = oldStatus;
    }

    public Status getOldStatus()
    {
        return oldStatus;
    }
}
