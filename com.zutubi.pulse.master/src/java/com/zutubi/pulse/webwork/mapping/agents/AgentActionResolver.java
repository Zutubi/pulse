package com.zutubi.pulse.webwork.mapping.agents;

import com.zutubi.pulse.webwork.mapping.PagedActionResolver;
import com.zutubi.pulse.webwork.mapping.ParameterisedActionResolver;
import com.zutubi.pulse.webwork.mapping.StaticMapActionResolver;

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
        addMapping("messages", new PagedActionResolver("agentMessages"));
        addMapping("info", new ParameterisedActionResolver("viewSystemInfo"));
    }
}
