package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.master.RecipeAssignmentRequest;
import com.zutubi.pulse.master.agent.AgentService;

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
