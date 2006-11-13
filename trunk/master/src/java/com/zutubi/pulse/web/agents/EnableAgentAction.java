package com.zutubi.pulse.web.agents;

import com.zutubi.pulse.model.Slave;

/**
 */
public class EnableAgentAction extends AgentActionSupport
{
    private String enable;

    public void setEnable(String enable)
    {
        this.enable = enable;
    }

    public String execute() throws Exception
    {
        lookupSlave();
        if(slave != null)
        {
            try
            {
                getAgentManager().setSlaveState(slave, Slave.EnableState.valueOf(enable));
            }
            catch(IllegalArgumentException e)
            {
                // Ignore invalid requests
            }
        }

        return SUCCESS;
    }
}
