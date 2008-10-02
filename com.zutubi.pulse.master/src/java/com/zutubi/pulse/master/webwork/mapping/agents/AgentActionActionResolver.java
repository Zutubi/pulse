package com.zutubi.pulse.master.webwork.mapping.agents;

import com.zutubi.pulse.master.webwork.mapping.ParameterisedActionResolver;

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
