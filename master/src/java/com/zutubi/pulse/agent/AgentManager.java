package com.zutubi.pulse.agent;

import com.zutubi.pulse.model.AgentState;
import com.zutubi.pulse.services.UpgradeStatus;

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

    void enableAgent(long handle);
    void disableAgent(long handle);
    void setAgentState(long handle, AgentState.EnableState state);

    void upgradeStatus(UpgradeStatus upgradeStatus);

    Agent getAgent(String name);

}
