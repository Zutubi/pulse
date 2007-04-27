package com.zutubi.pulse.web.agents;

/**
 */
public class PingAgentAction extends AgentActionSupport
{
    public String execute() throws Exception
    {
        getAgentManager().pingAgent(getAgent().getAgentConfig().getHandle());
        return SUCCESS;
    }
}
