package com.zutubi.pulse.web.agents;

import com.zutubi.pulse.model.AgentState;

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
        try
        {
            getAgentManager().setAgentState(getAgent().getAgentConfig().getHandle(), AgentState.EnableState.valueOf(enable));
        }
        catch(IllegalArgumentException e)
        {
            // Ignore invalid requests
        }
        return SUCCESS;
    }
}
