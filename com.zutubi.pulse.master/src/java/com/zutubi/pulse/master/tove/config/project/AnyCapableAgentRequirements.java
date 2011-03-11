package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.pulse.master.agent.AgentService;
import com.zutubi.pulse.master.build.queue.RecipeAssignmentRequest;
import com.zutubi.pulse.master.model.ResourceManager;

/**
 * Requirements that allow a stage to be dispatched to any agent that has the
 * required resources.
 */
public class AnyCapableAgentRequirements implements AgentRequirements
{
    private static final Messages I18N = Messages.getInstance(AnyCapableAgentRequirements.class);

    private ResourceManager resourceManager;

    public String getSummary()
    {
        return I18N.format("summary");
    }

    public boolean isFulfilledBy(RecipeAssignmentRequest request, AgentService service)
    {
        return resourceManager.getAgentRepository(service.getAgentConfig()).satisfies(request.getResourceRequirements());
    }

    public String getUnfulfilledReason(RecipeAssignmentRequest request)
    {
        StringBuffer message = new StringBuffer();
        message.append(I18N.format("unfulfilled.reason"));
        String sep = " ";
        for (ResourceRequirement requirement : request.getResourceRequirements())
        {
            if (!requirement.isOptional())
            {
                message.append(sep);
                message.append(requirement);
                sep = ", ";
            }
        }
        message.append(".");
        return message.toString();
    }

    public void setResourceManager(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }
}
