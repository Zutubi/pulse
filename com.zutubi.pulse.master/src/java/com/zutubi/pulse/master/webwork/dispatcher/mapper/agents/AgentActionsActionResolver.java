package com.zutubi.pulse.master.webwork.dispatcher.mapper.agents;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolverSupport;

/**
 */
public class AgentActionsActionResolver extends ActionResolverSupport
{
    public AgentActionsActionResolver()
    {
        super(null);
    }

    public ActionResolver getChild(String name)
    {
        return new AgentActionActionResolver(name);
    }
}
