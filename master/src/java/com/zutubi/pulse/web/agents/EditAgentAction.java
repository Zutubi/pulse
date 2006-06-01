/********************************************************************************
  @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.agents;

import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.model.SlaveManager;
import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.agent.AgentManager;

/**
 */
public class EditAgentAction extends ActionSupport
{
    private long id;
    private Slave slave = new Slave();
    private AgentManager agentManager;
    private SlaveManager slaveManager;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public Slave getSlave()
    {
        return slave;
    }

    public String doInput()
    {
        slave = slaveManager.getSlave(id);
        return INPUT;
    }

    public void validate()
    {
        Slave persistentSlave = slaveManager.getSlave(id);
        if(persistentSlave == null)
        {
            addActionError("Unknown agent [" + id + "]");
            return;
        }

        if(!slave.getName().equals(persistentSlave.getName()))
        {
            if(slaveManager.getSlave(slave.getName()) != null)
            {
                addFieldError("slave.name", "An agent with name '" + slave.getName() + "' already exists.");
            }
        }
    }

    public String execute()
    {
        Slave persistentSlave = slaveManager.getSlave(id);
        persistentSlave.setHost(slave.getHost());
        persistentSlave.setName(slave.getName());
        persistentSlave.setPort(slave.getPort());
        slaveManager.save(persistentSlave);
        agentManager.slaveChanged(persistentSlave.getId());
        return SUCCESS;
    }

    public void setSlaveManager(SlaveManager slaveManager)
    {
        this.slaveManager = slaveManager;
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }
}
