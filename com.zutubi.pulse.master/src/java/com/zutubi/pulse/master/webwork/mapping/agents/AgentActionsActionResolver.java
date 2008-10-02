package com.zutubi.pulse.master.webwork.mapping.agents;

import com.zutubi.pulse.master.webwork.mapping.ActionResolver;
import com.zutubi.pulse.master.webwork.mapping.ActionResolverSupport;

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
