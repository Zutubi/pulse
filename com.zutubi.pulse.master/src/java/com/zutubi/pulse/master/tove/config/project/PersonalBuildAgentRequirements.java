package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.master.build.queue.RecipeAssignmentRequest;
import com.zutubi.pulse.master.agent.AgentService;
import com.zutubi.i18n.Messages;

/**
 * Ensures that personal build assignment requests are only fulfilled by agents
 * which allow personal builds.
 */
public class PersonalBuildAgentRequirements implements AgentRequirements
{
    private static final Messages I18N = Messages.getInstance(PersonalBuildAgentRequirements.class);
    
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
        return I18N.format("summary", agentRequirements.getSummary());
    }

    public String getUnfulFilledReason(RecipeAssignmentRequest request)
    {
        return I18N.format("unfulfilled.reason", agentRequirements.getUnfulFilledReason(request));
    }

    public boolean isFulfilledBy(RecipeAssignmentRequest request, AgentService service)
    {
        return agentRequirements.isFulfilledBy(request, service) && verifyPersonalBuilds(request, service);
    }
}
