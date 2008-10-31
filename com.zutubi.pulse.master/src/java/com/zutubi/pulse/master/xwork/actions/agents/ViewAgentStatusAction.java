package com.zutubi.pulse.master.xwork.actions.agents;

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
