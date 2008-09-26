package com.zutubi.pulse.tove.config.project;

import com.zutubi.pulse.AgentService;
import com.zutubi.pulse.RecipeAssignmentRequest;

/**
 * Ensures that personal build assignment requests are only fulfilled by agents
 * which allow personal builds.
 */
public class PersonalBuildAgentRequirements implements AgentRequirements
{
    private AgentRequirements agentRequirements;

    public PersonalBuildAgentRequirements(AgentRequirements agentRequirements)
    {
        this.agentRequirements = agentRequirements;
    }

    private boolean verifyPersonalBuilds(RecipeAssignmentRequest request, AgentService service)
    {
        return !request.isPersonal() || service.getAgentConfig().getAllowPersonalBuilds();
    }

    public String getSummary()
    {
        return agentRequirements.getSummary() + " (personal build)";
    }

    public boolean fulfilledBy(RecipeAssignmentRequest request, AgentService service)
    {
        return agentRequirements.fulfilledBy(request, service) && verifyPersonalBuilds(request, service);
    }
}
