package com.zutubi.pulse.master.agent;

import com.zutubi.pulse.master.build.queue.Prioritiser;
import com.zutubi.pulse.master.build.queue.RecipeAssignmentRequest;

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
