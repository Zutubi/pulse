package com.zutubi.pulse.master.agent;

import com.zutubi.pulse.master.model.AgentState;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.servercore.agent.Status;
import com.zutubi.pulse.servercore.services.SlaveStatus;
import com.zutubi.pulse.servercore.services.UpgradeState;

/**
 * An abstraction over an agent, be it the local (master) agent or a slave.
 */
public interface Agent
{
    String getName();
    Status getStatus();
    String getLocation();
    UpgradeState getUpgradeState();

    long getId();

    void updateStatus(SlaveStatus status);
    void updateStatus(Status status);
    void updateStatus(Status status, long recipeId);

    void copyStatus(Agent agent);

    void upgradeStatus(UpgradeState state, int progress, String message);

    AgentConfiguration getConfig();
    AgentService getService();

    long getSecondsSincePing();

    long getRecipeId();
    void setRecipeId(long recipeId);

    boolean isOnline();
    boolean isEnabled();
    boolean isDisabling();
    boolean isDisabled();
    boolean isUpgrading();
    boolean isFailedUpgrade();
    boolean isAvailable();

    AgentState.EnableState getEnableState();

    void setAgentState(AgentState agentState);
}
