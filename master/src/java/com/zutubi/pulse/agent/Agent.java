package com.zutubi.pulse.agent;

import com.zutubi.pulse.AgentService;
import com.zutubi.pulse.SystemInfo;
import com.zutubi.pulse.prototype.config.agent.AgentConfiguration;
import com.zutubi.pulse.services.SlaveStatus;
import com.zutubi.pulse.services.UpgradeState;
import com.zutubi.pulse.logging.CustomLogRecord;
import com.zutubi.pulse.model.NamedEntity;
import com.zutubi.pulse.model.AgentState;

import java.util.List;

/**
 * An abstraction over an agent, be it the local (master) agent or a slave.
 */
public interface Agent
{
    Status getStatus();
    String getLocation();

    void updateStatus(SlaveStatus status);
    void setStatus(Status status);
    void upgradeStatus(UpgradeState state, int progress, String message);

    AgentConfiguration getAgentConfig();
    AgentState getAgentState();
    AgentService getService();

    boolean isOnline();
    boolean isEnabled();
    boolean isUpgrading();
    boolean isFailedUpgrade();
    boolean isAvailable();

}
