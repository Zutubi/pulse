package com.zutubi.pulse.events;

import com.zutubi.pulse.agent.Agent;
import com.zutubi.pulse.model.Slave;

/**
 * Raised when an agent's persistent enable state should be changed to
 * a given state.
 */
public class AgentEnableStateRequiredEvent extends AgentEvent
{
    private Slave.EnableState state;

    public AgentEnableStateRequiredEvent(Object source, Agent agent, Slave.EnableState state)
    {
        super(source, agent);
        this.state = state;
    }

    public Slave.EnableState getState()
    {
        return state;
    }

    public String toString()
    {
        return ("Agent Enable State Required Event: " + getAgent().getName() + ": " + state.toString());
    }
}
