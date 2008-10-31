package com.zutubi.pulse.master;

import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.util.Sort;
import org.mortbay.util.Credential;

import java.util.Comparator;

/**
 * A Comparator to sort Agents in some arbitrary but deterministic order based
 * on the the Agent, Project, and Spec names.  This should improve performance
 * of incremental builds.
 *
 */
public class Prioritiser implements Comparator<Agent>
{
    private final RecipeAssignmentRequest request;

    public Prioritiser(RecipeAssignmentRequest request)
    {
        this.request = request;
    }

    public int compare(Agent agent1, Agent agent2)
    {
        Sort.StringComparator stringComparator = new Sort.StringComparator();
        return stringComparator.compare(getAgentHash(agent1), getAgentHash(agent2));
    }

    private String getAgentHash(Agent agent)
    {
        return Credential.MD5.digest(agent.getName() + request.getBuild().getProject().getName());
    }
}
