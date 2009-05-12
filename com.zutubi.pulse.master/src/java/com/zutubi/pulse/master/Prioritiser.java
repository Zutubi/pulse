package com.zutubi.pulse.master;

import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.util.Sort;
import org.mortbay.util.Credential;

import java.util.Comparator;

/**
 * A Comparator to sort agents by:
 *
 * <ul>
 *     <li>the agent priority; and for those of the same priority</li>
 *     <li>an arbitrary but deterministic order based the agent and project
 *         names (this should improve performance of incremental builds).</li>
 * </ul>
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
        int result = agent2.getConfig().getPriority() - agent1.getConfig().getPriority();
        if (result == 0)
        {
            Sort.StringComparator stringComparator = new Sort.StringComparator();
            result = stringComparator.compare(getAgentHash(agent1), getAgentHash(agent2));
        }

        return result;
    }

    private String getAgentHash(Agent agent)
    {
        return Credential.MD5.digest(agent.getName() + request.getBuild().getProject().getName());
    }
}
