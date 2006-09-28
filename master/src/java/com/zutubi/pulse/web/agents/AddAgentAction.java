package com.zutubi.pulse.web.agents;

import com.zutubi.pulse.agent.AgentManager;
import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.model.SlaveManager;
import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.license.Licensed;
import com.zutubi.pulse.license.LicenseHolder;
import com.zutubi.pulse.license.LicenseException;

/**
 * 
 *
 */
@Licensed(LicenseHolder.AUTH_ADD_AGENT)
public class AddAgentAction extends ActionSupport
{
    private Slave slave = new Slave();
    private AgentManager agentManager;

    public Slave getSlave()
    {
        return slave;
    }

    public void validate()
    {
        if (hasErrors())
        {
            // do not attempt to validate unless all other validation rules have 
            // completed successfully.
            return;
        }

        if (agentManager.agentExists(slave.getName()))
        {
            // slave name already in use.
            addFieldError("slave.name", "An agent with name '" + slave.getName() + "' already exists.");
        }
    }

    public String execute() throws LicenseException
    {
        agentManager.addSlave(slave);
        return SUCCESS;
    }

    public String doDefault()
    {
        // setup any default data.
        return SUCCESS;
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }
}
