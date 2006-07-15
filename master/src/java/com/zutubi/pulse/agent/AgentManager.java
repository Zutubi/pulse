package com.zutubi.pulse.agent;

import com.zutubi.pulse.model.Slave;

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
}
