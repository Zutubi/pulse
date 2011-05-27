package com.zutubi.pulse.master.webwork.dispatcher.mapper.agents;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolverSupport;

import java.util.Arrays;
import java.util.List;

/**
 */
public class AgentsActionResolver extends ActionResolverSupport
{
    public AgentsActionResolver()
    {
        super("agents");
    }

    public List<String> listChildren()
    {
        return Arrays.asList("<agent>");
    }

    public ActionResolver getChild(String name)
    {
        return new AgentActionResolver(name);
    }
}
