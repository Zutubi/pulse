package com.zutubi.pulse.model;

import com.zutubi.pulse.BuildService;
import com.zutubi.pulse.RecipeDispatchRequest;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.core.PulseException;
import com.zutubi.pulse.core.PulseFileLoader;
import com.zutubi.pulse.core.PulseFileLoaderFactory;
import com.zutubi.util.logging.Logger;

import java.util.List;

/**
 */
public class AnyCapableBuildHostRequirements extends AbstractBuildHostRequirements
{
    private static final Logger LOG = Logger.getLogger(AnyCapableBuildHostRequirements.class);

    private PulseFileLoaderFactory fileLoaderFactory;

    public AnyCapableBuildHostRequirements()
    {
        ComponentContext.autowire(this);
    }

    public BuildHostRequirements copy()
    {
        return new AnyCapableBuildHostRequirements();
    }

    public boolean fulfilledBy(RecipeDispatchRequest request, BuildService service)
    {
        List<ResourceRequirement> requirements = request.getRequest().getResourceRequirements();
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
            LOG.warning("Unable to load resource requirements from pulse file: " + e.getMessage(), e);
        }

        return true;
    }

    public String getSummary()
    {
        return "[any]";
    }

    public void setFileLoaderFactory(PulseFileLoaderFactory fileLoaderFactory)
    {
        this.fileLoaderFactory = fileLoaderFactory;
    }
}
