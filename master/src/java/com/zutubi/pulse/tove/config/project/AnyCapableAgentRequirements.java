package com.zutubi.pulse.tove.config.project;

import com.zutubi.pulse.AgentService;
import com.zutubi.pulse.RecipeDispatchRequest;
import com.zutubi.pulse.core.PulseException;
import com.zutubi.pulse.core.PulseFileLoader;
import com.zutubi.pulse.core.PulseFileLoaderFactory;
import com.zutubi.pulse.model.ResourceRequirement;
import com.zutubi.util.logging.Logger;

import java.util.List;

/**
 * Requirements that allow a stage to be dispatched to any agent that has the
 * required resources.
 */
public class AnyCapableAgentRequirements implements AgentRequirements
{
    private static final Logger LOG = Logger.getLogger(AnyCapableAgentRequirements.class);

    private PulseFileLoaderFactory fileLoaderFactory;

    public AnyCapableAgentRequirements()
    {
    }

    public boolean fulfilledBy(RecipeDispatchRequest request, AgentService service)
    {
        List<ResourceRequirement> requirements = request.getResourceRequirements();
        for(ResourceRequirement requirement: requirements)
        {
            if(!service.hasResource(requirement.getResource(), requirement.getVersion()))
            {
                return false;
            }
        }

        PulseFileLoader fileLoader = fileLoaderFactory.createLoader();
        try
        {
            requirements = fileLoader.loadRequiredResources(request.getRevision().getPulseFile(), request.getRequest().getRecipeName());
            for(ResourceRequirement requirement: requirements)
            {
                if(!service.hasResource(requirement.getResource(), requirement.getVersion()))
                {
                    return false;
                }
            }
        }
        catch (PulseException e)
        {
            // Continue, assuming no further requirements.
            LOG.warning("Unable to load resource requirements from pulse file for project '" + request.getProject().getName() + "': " + e.getMessage(), e);
        }

        return true;
    }

    public void setFileLoaderFactory(PulseFileLoaderFactory fileLoaderFactory)
    {
        this.fileLoaderFactory = fileLoaderFactory;
    }
}
