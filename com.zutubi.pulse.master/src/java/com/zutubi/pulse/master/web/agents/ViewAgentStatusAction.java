package com.zutubi.pulse.master.web.agents;

/**
 */
public class ViewAgentStatusAction extends AgentActionBase
{
    public String execute() throws Exception
    {
        getRequiredAgent();
        return SUCCESS;
    }
}
