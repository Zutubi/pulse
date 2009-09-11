package com.zutubi.pulse.master.agent;

import com.zutubi.pulse.master.events.AgentPingEvent;
import com.zutubi.pulse.master.model.AgentState;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;

/**
 * An abstraction over an agent, be it the local (master) agent or a slave.
 */
public interface Agent
{
    String getName();
    AgentStatus getStatus();
    Host getHost();

    long getId();

    void updateStatus(AgentPingEvent agentPingEvent);
    void updateStatus(AgentStatus status);
    void updateStatus(AgentStatus status, long recipeId);

    void copyStatus(Agent agent);

    AgentConfiguration getConfig();
    AgentService getService();

    long getSecondsSincePing();

    long getRecipeId();

    boolean isOnline();
    boolean isEnabled();
    boolean isDisabling();
    boolean isDisabled();
    boolean isAvailable();

    AgentState.EnableState getEnableState();

    void setAgentState(AgentState agentState);
}
