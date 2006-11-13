package com.zutubi.pulse.web.agents;

import com.zutubi.pulse.model.Slave;

/**
 */
public class PingAgentAction extends AgentActionSupport
{
    public String execute() throws Exception
    {
        lookupSlave();
        Slave slave = getSlave();
        if(slave != null)
        {
            getAgentManager().pingSlave(slave);
        }

        return SUCCESS;
    }
}
