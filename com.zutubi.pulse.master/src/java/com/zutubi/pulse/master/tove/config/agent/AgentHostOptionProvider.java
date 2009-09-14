package com.zutubi.pulse.master.tove.config.agent;

import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.tove.handler.ListOptionProvider;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.NotNullPredicate;
import com.zutubi.util.Sort;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Lists currently-used agent hosts as an extra hint to the user that they can
 * run more than one agent on a single host.
 */
public class AgentHostOptionProvider extends ListOptionProvider
{
    private AgentManager agentManager;

    public String getEmptyOption(Object instance, String parentPath, TypeProperty property)
    {
        return "";
    }

    public List<String> getOptions(Object instance, String parentPath, TypeProperty property)
    {
        Set<String> uniqueHosts = new TreeSet<String>(new Sort.StringComparator());
        CollectionUtils.filter(CollectionUtils.map(agentManager.getAllAgents(), new Mapping<Agent, String>()
        {
            public String map(Agent agent)
            {
                return agent.getConfig().getHost();
            }
        }), new NotNullPredicate<String>(), uniqueHosts);

        return new LinkedList<String>(uniqueHosts);
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }
}
