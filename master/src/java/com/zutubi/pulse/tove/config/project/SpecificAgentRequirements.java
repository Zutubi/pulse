package com.zutubi.pulse.tove.config.project;

import com.zutubi.pulse.AgentService;
import com.zutubi.pulse.RecipeDispatchRequest;
import com.zutubi.pulse.tove.config.agent.AgentConfiguration;

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

    public boolean fulfilledBy(RecipeDispatchRequest request, AgentService service)
    {
        return service.getAgentConfig().equals(agent);
    }
}
