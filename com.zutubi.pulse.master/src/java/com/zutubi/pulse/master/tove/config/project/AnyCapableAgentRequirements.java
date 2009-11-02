package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.core.config.ResourceRequirement;
import com.zutubi.pulse.master.build.queue.RecipeAssignmentRequest;
import com.zutubi.pulse.master.agent.AgentService;

import java.util.List;

/**
 * Requirements that allow a stage to be dispatched to any agent that has the
 * required resources.
 */
public class AnyCapableAgentRequirements implements AgentRequirements
{
    public AnyCapableAgentRequirements()
    {
    }

    public String getSummary()
    {
        return "[any]";
    }

    public boolean fulfilledBy(RecipeAssignmentRequest request, AgentService service)
    {
        List<ResourceRequirement> requirements = request.getResourceRequirements();
        for(ResourceRequirement requirement: requirements)
        {
            if (!requirement.isOptional() && !service.hasResource(requirement))
            {
                return false;
            }
        }

        return true;
    }
}
