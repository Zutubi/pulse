package com.zutubi.pulse.web.agents;

/**
 */
public class EnableAgentAction extends AgentActionSupport
{
    private boolean enable;

    public void setEnable(boolean enable)
    {
        this.enable = enable;
    }

    public String execute() throws Exception
    {
        lookupSlave();
        if(slave != null)
        {
            slave.setEnabled(enable);
            getSlaveManager().save(slave);
            getAgentManager().slaveChanged(getAgentId());
        }

        return SUCCESS;
    }
}
