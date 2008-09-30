package com.zutubi.pulse.agent;

import com.zutubi.pulse.Prioritiser;
import com.zutubi.pulse.RecipeAssignmentRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Agent sorter which uses Prioritiser to sort the agent list.
 */
public class DefaultAgentSorter implements AgentSorter
{
    public Iterable<Agent> sort(Collection<Agent> agents, RecipeAssignmentRequest request)
    {
        List<Agent> agentList = new ArrayList<Agent>(agents);
        Collections.sort(agentList, new Prioritiser(request));
        return agentList;
    }
}
