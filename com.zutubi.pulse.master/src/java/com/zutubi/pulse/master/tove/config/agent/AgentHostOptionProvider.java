package com.zutubi.pulse.master.tove.config.agent;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.tove.handler.FormContext;
import com.zutubi.pulse.master.tove.handler.ListOptionProvider;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.util.Sort;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.google.common.collect.Iterables.*;

/**
 * Lists currently-used agent hosts as an extra hint to the user that they can
 * run more than one agent on a single host.
 */
public class AgentHostOptionProvider extends ListOptionProvider
{
    private AgentManager agentManager;

    public String getEmptyOption(TypeProperty property, FormContext context)
    {
        return "";
    }

    public List<String> getOptions(TypeProperty property, FormContext context)
    {
        Set<String> uniqueHosts = new TreeSet<String>(new Sort.StringComparator());
        addAll(uniqueHosts, filter(transform(agentManager.getAllAgents(), new Function<Agent, String>()
        {
            public String apply(Agent agent)
            {
                return agent.getConfig().getHost();
            }
        }), Predicates.notNull()));

        return new LinkedList<String>(uniqueHosts);
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }
}
