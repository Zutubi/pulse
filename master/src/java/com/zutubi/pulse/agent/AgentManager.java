package com.zutubi.pulse.agent;

import com.zutubi.pulse.license.LicenseException;
import com.zutubi.pulse.model.AgentState;
import com.zutubi.pulse.services.UpgradeStatus;
import com.zutubi.pulse.prototype.config.agent.AgentConfiguration;

import java.util.List;

/**
 */
public interface AgentManager
{
    List<Agent> getAllAgents();
    List<Agent> getOnlineAgents();
    Agent getAgent(long handle);

    void pingAgent(long handle);
    void pingAgents();

    int getAgentCount();

    void addAgent(AgentConfiguration agentConfig) throws LicenseException;
    void enableAgent(long handle);
    void disableAgent(long handle);
    void setAgentState(long handle, AgentState.EnableState state);

    void upgradeStatus(UpgradeStatus upgradeStatus);

    Agent getAgent(String name);

}
