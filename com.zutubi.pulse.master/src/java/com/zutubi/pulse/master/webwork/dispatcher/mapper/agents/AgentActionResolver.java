package com.zutubi.pulse.master.webwork.dispatcher.mapper.agents;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.PagedActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ParameterisedActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.StaticMapActionResolver;

/**
 */
public class AgentActionResolver extends StaticMapActionResolver
{
    public AgentActionResolver(String agent)
    {
        super("agentStatus");
        addParameter("agentName", agent);
        addMapping("actions", new AgentActionsActionResolver());
        addMapping("status", new ParameterisedActionResolver("agentStatus"));
        addMapping("statistics", new ParameterisedActionResolver("agentStatistics"));
        addMapping("history", new PagedActionResolver("agentHistory"));
        addMapping("messages", new PagedActionResolver("serverMessages"));
        addMapping("info", new ParameterisedActionResolver("serverInfo"));
    }
}
