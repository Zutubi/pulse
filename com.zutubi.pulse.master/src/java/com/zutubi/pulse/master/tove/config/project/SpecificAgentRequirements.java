package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.master.build.queue.RecipeAssignmentRequest;
import com.zutubi.pulse.master.agent.AgentService;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.i18n.Messages;

/**
 * Requirements that only allow a stage to be dispatch to a specified agent.
 */
public class SpecificAgentRequirements implements AgentRequirements
{
    private static final Messages I18N  = Messages.getInstance(SpecificAgentRequirements.class);
    
    private AgentConfiguration agent;

    public SpecificAgentRequirements(AgentConfiguration agent)
    {
        this.agent = agent;
    }

    public String getSummary()
    {
        return I18N.format("summary", agent.getName());
    }

    public boolean isFulfilledBy(RecipeAssignmentRequest request, AgentService service)
    {
        return service.getAgentConfig().equals(agent);
    }

    public String getUnfulFilledReason(RecipeAssignmentRequest request)
    {
        return I18N.format("unfulfilled.reason", agent.getName());
    }
}
