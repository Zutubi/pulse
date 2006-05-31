package com.zutubi.pulse.web.agents;

/**
 */
public class ViewAgentStatusAction extends AgentActionSupport
{
    public String execute() throws Exception
    {
        lookupSlave();
        return SUCCESS;
    }
}
