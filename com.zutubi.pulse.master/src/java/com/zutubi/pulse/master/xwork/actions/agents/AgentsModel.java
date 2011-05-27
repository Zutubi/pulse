package com.zutubi.pulse.master.xwork.actions.agents;

import flexjson.JSON;

import java.util.LinkedList;
import java.util.List;

/**
 */
public class AgentsModel
{
    private List<AgentRowModel> agents = new LinkedList<AgentRowModel>();
    private List<String> invalidAgents = new LinkedList<String>();

    @JSON
    public List<AgentRowModel> getAgents()
    {
        return agents;
    }

    public void addAgents(List<AgentRowModel> agents)
    {
        this.agents.addAll(agents);
    }

    @JSON
    public List<String> getInvalidAgents()
    {
        return invalidAgents;
    }

    public void addInvalidAgents(List<String> names)
    {
        invalidAgents.addAll(names);
    }
}
