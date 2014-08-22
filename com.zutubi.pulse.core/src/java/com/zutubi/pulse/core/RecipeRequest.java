package com.zutubi.pulse.core;

import com.zutubi.pulse.core.engine.PulseFileProvider;
import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import com.zutubi.pulse.core.engine.api.ResourceProperty;
import com.zutubi.pulse.core.resources.ResourceRequirement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * A request to execute a specific recipe.  Includes details about how to
 * bootstrap this step of the build (e.g. by SCM checkout, or by using a
 * working directory left by a previous recipe).
 */
public class RecipeRequest
{
    /**
     * Used to bootstrap the working directory.
     */
    private Bootstrapper bootstrapper;
    /**
     * The pulse file, potentially set lazily as it is determined (when
     * revision is determined).
     */
    private PulseFileProvider pulseFileProvider;
    /**
     * Context for the recipe.
     */
    private PulseExecutionContext context;

    private List<ResourceRequirement> resourceRequirements = new LinkedList<ResourceRequirement>();

    private List<ResourceProperty> properties = new LinkedList<ResourceProperty>();

    public RecipeRequest(PulseExecutionContext context)
    {
        this(null, null, context);
    }

    public RecipeRequest(Bootstrapper bootstrapper, PulseFileProvider pulseFileProvider, PulseExecutionContext context)
    {
        this.bootstrapper = bootstrapper;
        this.pulseFileProvider = pulseFileProvider;
        this.context = context;
    }

    /**
     * Get the name of the project associated with this recipe execution request.
     *
     * @return the project name.
     */
    public String getProject()
    {
        return context.getString(NAMESPACE_INTERNAL, PROPERTY_PROJECT);
    }

    /**
     * Get the organisation of the project associated with this recipe execution request.
     *
     * @return the projects organisation.
     */
    public String getProjectOrg()
    {
        return context.getString(NAMESPACE_INTERNAL, PROPERTY_ORGANISATION);
    }

    /**
     * Get the build number of the build that this recipe execution request belongs to.
     * @return
     */
    public String getBuildNumber()
    {
        return context.getString(NAMESPACE_INTERNAL,PROPERTY_BUILD_NUMBER);
    }

    public long getBuildId()
    {
        return context.getLong(NAMESPACE_INTERNAL, PROPERTY_BUILD_ID, 0);
    }
    
    public long getId()
    {
        return context.getLong(NAMESPACE_INTERNAL, PROPERTY_RECIPE_ID, 0);
    }

    public Bootstrapper getBootstrapper()
    {
        return bootstrapper;
    }

    public PulseFileProvider getPulseFileSource()
    {
        return pulseFileProvider;
    }

    public String getRecipeName()
    {
        return context.getString(NAMESPACE_INTERNAL, PROPERTY_RECIPE);
    }

    /**
     * Get the name of the build stage in which this recipe execution request is being processed.
     *
     * @return the name of the build stage.
     */
    public String getStageName()
    {
        return context.getString(NAMESPACE_INTERNAL, PROPERTY_STAGE);
    }

    public String getRecipeNameSafe()
    {
        String recipe = getRecipeName();
        if(recipe == null)
        {
            return "[default]";
        }
        else
        {
            return recipe;
        }
    }

    public void setBootstrapper(Bootstrapper bootstrapper)
    {
        this.bootstrapper = bootstrapper;
    }

    public void setPulseFileSource(PulseFileProvider pulseFileProvider)
    {
        this.pulseFileProvider = pulseFileProvider;
    }

    public PulseExecutionContext getContext()
    {
        return context;
    }

    public List<ResourceRequirement> getResourceRequirements()
    {
        return resourceRequirements;
    }

    public void addAllResourceRequirements(Collection<? extends ResourceRequirement> requirements)
    {
        resourceRequirements.addAll(requirements);
    }

    public List<ResourceProperty> getProperties()
    {
        return properties;
    }

    public void addAllProperties(Collection<? extends ResourceProperty> properties)
    {
        this.properties.addAll(properties);
    }
}
