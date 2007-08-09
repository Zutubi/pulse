package com.zutubi.pulse.agent;

import com.zutubi.pulse.AgentService;
import com.zutubi.pulse.model.AgentState;
import com.zutubi.pulse.prototype.config.agent.AgentConfiguration;
import com.zutubi.pulse.services.SlaveStatus;
import com.zutubi.pulse.services.UpgradeState;

/**
 * An abstraction over an agent, be it the local (master) agent or a slave.
 */
public interface Agent
{
    Status getStatus();
    String getLocation();

    long getId();

    void updateStatus(SlaveStatus status);
    void setStatus(Status status);
    void upgradeStatus(UpgradeState state, int progress, String message);

    AgentConfiguration getConfig();
    AgentService getService();

    boolean isOnline();
    boolean isEnabled();
    boolean isDisabled();
    boolean isUpgrading();
    boolean isFailedUpgrade();
    boolean isAvailable();

    AgentState.EnableState getEnableState();

    void setAgentState(AgentState agentState);
}
