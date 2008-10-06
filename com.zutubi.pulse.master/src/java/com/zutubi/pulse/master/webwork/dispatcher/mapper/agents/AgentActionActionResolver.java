package com.zutubi.pulse.master.webwork.dispatcher.mapper.agents;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ParameterisedActionResolver;

/**
 */
public class AgentActionActionResolver extends ParameterisedActionResolver
{
    public AgentActionActionResolver(String action)
    {
        super("agentAction");
        addParameter("action", action);
    }
}
