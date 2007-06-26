package com.zutubi.pulse.events;

import com.zutubi.pulse.agent.Status;
import com.zutubi.pulse.agent.Agent;

/**
 */
public class AgentStatusEvent extends AgentEvent
{
    private Status oldStatus;

    public AgentStatusEvent(Object source, Status oldStatus, Agent agent)
    {
        super(source, agent);
        this.oldStatus = oldStatus;
    }

    public Status getOldStatus()
    {
        return oldStatus;
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Agent Status Event");
        if (getAgent() != null)
        {
            buff.append(": ").append(getAgent().getConfig().getName()).append(", ").append(oldStatus);
        }
        return buff.toString();
    }    
}
