package com.zutubi.pulse.web.agents;

import com.zutubi.pulse.model.SlaveManager;
import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.web.ActionSupport;

/**
 */
public class AgentActionSupport extends ActionSupport
{
    private long agentId;
    private SlaveManager slaveManager;
    protected Slave slave;

    public long getAgentId()
    {
        return agentId;
    }

    public void setAgentId(long agentId)
    {
        this.agentId = agentId;
    }

    protected void lookupSlave()
    {
        slave = slaveManager.getSlave(agentId);
    }

    public void setSlaveManager(SlaveManager slaveManager)
    {
        this.slaveManager = slaveManager;
    }

    public Slave getSlave()
    {
        return slave;
    }
}
