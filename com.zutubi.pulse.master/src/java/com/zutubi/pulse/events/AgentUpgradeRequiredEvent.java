package com.zutubi.pulse.events;

import com.zutubi.pulse.master.agent.Agent;

/**
 * Raised when an agent reports a version mismatch.
 */
public class AgentUpgradeRequiredEvent extends AgentEvent
{
    public AgentUpgradeRequiredEvent(Object source, Agent agent)
    {
        super(source, agent);
    }

    public String toString()
    {
        return ("Agent Upgrade Required Event: " + getAgent().getConfig().getName());
    }
}
