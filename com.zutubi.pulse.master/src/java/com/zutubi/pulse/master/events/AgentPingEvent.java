package com.zutubi.pulse.master.events;

import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.servercore.services.SlaveStatus;

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
            buff.append(": ").append(getAgent().getConfig().getName()).append(", ").append(pingStatus);
        }
        return buff.toString();
    }
}
