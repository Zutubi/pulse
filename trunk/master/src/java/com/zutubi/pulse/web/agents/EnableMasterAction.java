package com.zutubi.pulse.web.agents;

/**
 * <class comment/>
 */
public class EnableMasterAction extends AgentActionSupport
{
    private boolean enable;


    public void setEnable(boolean enable)
    {
        this.enable = enable;
    }


    public String execute() throws Exception
    {
        if (enable)
        {
            getAgentManager().enableMasterAgent();
        }
        else
        {
            getAgentManager().disableMasterAgent();
        }
        return SUCCESS;
    }
}
