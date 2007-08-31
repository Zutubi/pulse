package com.zutubi.pulse.webwork.mapping.agents;

import com.zutubi.pulse.webwork.mapping.ActionResolver;
import com.zutubi.pulse.webwork.mapping.ActionResolverSupport;

/**
 */
public class AgentsActionResolver extends ActionResolverSupport
{
    public AgentsActionResolver()
    {
        super("viewAgents");
    }

    public ActionResolver getChild(String name)
    {
        return new AgentActionResolver(name);
    }
}
