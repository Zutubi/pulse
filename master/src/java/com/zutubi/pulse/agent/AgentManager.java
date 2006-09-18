package com.zutubi.pulse.agent;

import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.services.UpgradeState;
import com.zutubi.pulse.services.UpgradeStatus;

import java.util.List;

/**
 */
public interface AgentManager
{
    List<Agent> getAllAgents();
    List<Agent> getOnlineAgents();
    Agent getAgent(Slave slave);

    void pingSlave(Slave slave);
    void pingSlaves();

    int getAgentCount();

    void slaveAdded(long id);
    void slaveChanged(long id);
    void slaveDeleted(long id);

    void upgradeStatus(UpgradeStatus upgradeStatus);
}
