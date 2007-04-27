package com.zutubi.pulse.events;

import com.zutubi.pulse.agent.Agent;

/**
 */
public class AgentUpgradeCompleteEvent extends AgentEvent
{
    private boolean successful;

    public AgentUpgradeCompleteEvent(Object source, Agent agent, boolean successful)
    {
        super(source, agent);
        this.successful = successful;
    }

    public boolean isSuccessful()
    {
        return successful;
    }
}
