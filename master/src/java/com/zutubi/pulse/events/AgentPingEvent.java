package com.zutubi.pulse.events;

import com.zutubi.pulse.agent.Status;
import com.zutubi.pulse.agent.Agent;
import com.zutubi.pulse.services.SlaveStatus;

/**
 */
public class AgentPingEvent extends AgentEvent
{
    private SlaveStatus pingStatus;

    public AgentPingEvent(Object source, Agent agent, SlaveStatus pingStatus)
    {
        super(source, agent);
        this.pingStatus = pingStatus;
    }

    public SlaveStatus getPingStatus()
    {
        return pingStatus;
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Agent Ping Event");
        if (getAgent() != null)
        {
            buff.append(": ").append(getAgent().getName()).append(", ").append(pingStatus);
        }
        return buff.toString();
    }
}
