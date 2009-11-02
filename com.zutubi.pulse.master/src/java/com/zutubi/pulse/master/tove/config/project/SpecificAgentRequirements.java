package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.master.build.queue.RecipeAssignmentRequest;
import com.zutubi.pulse.master.agent.AgentService;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;

/**
 * Requirements that only allow a stage to be dispatch to a specified agent.
 */
public class SpecificAgentRequirements implements AgentRequirements
{
    private AgentConfiguration agent;

    public SpecificAgentRequirements(AgentConfiguration agent)
    {
        this.agent = agent;
    }

    public String getSummary()
    {
        return agent.getName();
    }

    public boolean fulfilledBy(RecipeAssignmentRequest request, AgentService service)
    {
        return service.getAgentConfig().equals(agent);
    }
}
