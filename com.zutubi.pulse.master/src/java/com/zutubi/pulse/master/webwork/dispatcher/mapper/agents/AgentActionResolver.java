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
        super("viewAgentStatus");
        addParameter("agentName", agent);
        addMapping("actions", new AgentActionsActionResolver());
        addMapping("status", new ParameterisedActionResolver("viewAgentStatus"));
        addMapping("statistics", new ParameterisedActionResolver("agentStatistics"));
        addMapping("messages", new PagedActionResolver("agentMessages"));
        addMapping("info", new ParameterisedActionResolver("viewSystemInfo"));
    }
}
