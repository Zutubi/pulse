package com.zutubi.pulse.web.agents;

import com.zutubi.pulse.agent.Agent;
import com.zutubi.pulse.agent.AgentManager;
import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.model.SlaveManager;
import com.zutubi.pulse.web.ActionSupport;

/**
 */
public class AgentActionSupport extends ActionSupport
{
    public static String AGENT_ERROR = "agenterror";

    private long agentId;
    private AgentManager agentManager;
    private SlaveManager slaveManager;
    protected Slave slave;
    protected Agent agent;

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

    public SlaveManager getSlaveManager()
    {
        return slaveManager;
    }

    public void setSlaveManager(SlaveManager slaveManager)
    {
        this.slaveManager = slaveManager;
    }

    public Slave getSlave()
    {
        return slave;
    }

    public Agent getAgent()
    {
        if(agent == null)
        {
            agent = agentManager.getAgent(slave);
        }
        return agent;
    }

    public AgentManager getAgentManager()
    {
        return agentManager;
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }
}
