package com.zutubi.pulse.agent;

import com.zutubi.pulse.license.LicenseException;
import com.zutubi.pulse.model.Slave;
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

    void addSlave(Slave slave) throws LicenseException;
    void enableSlave(long slaveId);
    void disableSlave(long slaveId);
    void setSlaveState(long slaveId, Slave.EnableState state);

    void slaveAdded(long id);
    void slaveChanged(long id);
    void slaveDeleted(long id);

    void upgradeStatus(UpgradeStatus upgradeStatus);

    boolean agentExists(String name);

    Agent getAgent(String name);

    void enableMasterAgent();

    void disableMasterAgent();
}
