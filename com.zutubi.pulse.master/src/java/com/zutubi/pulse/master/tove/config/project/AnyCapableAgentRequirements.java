package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.master.agent.AgentService;
import com.zutubi.pulse.master.build.queue.RecipeAssignmentRequest;
import com.zutubi.pulse.master.model.ResourceManager;

/**
 * Requirements that allow a stage to be dispatched to any agent that has the
 * required resources.
 */
public class AnyCapableAgentRequirements implements AgentRequirements
{
    private ResourceManager resourceManager;

    public String getSummary()
    {
        return "[any]";
    }

    public boolean fulfilledBy(RecipeAssignmentRequest request, AgentService service)
    {
        return resourceManager.getAgentRepository(service.getAgentConfig()).satisfies(request.getResourceRequirements());
    }

    public void setResourceManager(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }
}
