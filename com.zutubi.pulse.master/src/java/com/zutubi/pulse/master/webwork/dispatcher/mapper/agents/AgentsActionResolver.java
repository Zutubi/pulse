package com.zutubi.pulse.master.webwork.dispatcher.mapper.agents;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolverSupport;

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
