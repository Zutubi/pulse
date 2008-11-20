package com.zutubi.pulse.master.webwork.dispatcher.mapper.agents;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolverSupport;

import java.util.Arrays;
import java.util.List;

/**
 */
public class AgentActionsActionResolver extends ActionResolverSupport
{
    public AgentActionsActionResolver()
    {
        super(null);
    }

    public List<String> listChildren()
    {
        return Arrays.asList("<agent action>");
    }

    public ActionResolver getChild(String name)
    {
        return new AgentActionActionResolver(name);
    }
}
