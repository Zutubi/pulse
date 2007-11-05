package com.zutubi.pulse.core;

import com.zutubi.pulse.model.ResourceRequirement;

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
    private String pulseFileSource;
    /**
     * Required resources for the build.  If the pulse file is set lazily,
     * some requirements may be added at that time.
     */
    private List<ResourceRequirement> resourceRequirements;
    /**
     * Context for the recipe.
     */
    private ExecutionContext context;

    public RecipeRequest(List<ResourceRequirement> resourceRequirements, ExecutionContext context)
    {
        this(null, null, resourceRequirements, context);
    }

    public RecipeRequest(Bootstrapper bootstrapper, String pulseFileSource, ExecutionContext context)
    {
        this(bootstrapper, pulseFileSource, null, context);
    }

    public RecipeRequest(Bootstrapper bootstrapper, String pulseFileSource, List<ResourceRequirement> resourceRequirements, ExecutionContext context)
    {
        this.bootstrapper = bootstrapper;
        this.pulseFileSource = pulseFileSource;
        this.resourceRequirements = resourceRequirements;
        this.context = context;
    }

    public String getProject()
    {
        return context.getInternalString(BuildProperties.PROPERTY_PROJECT);
    }

    public long getId()
    {
        return context.getInternalLong(BuildProperties.PROPERTY_RECIPE_ID);
    }

    public Bootstrapper getBootstrapper()
    {
        return bootstrapper;
    }

    public String getPulseFileSource()
    {
        return pulseFileSource;
    }

    public String getRecipeName()
    {
        return context.getInternalString(BuildProperties.PROPERTY_RECIPE);
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

    public void setPulseFileSource(String pulseFileSource)
    {
        this.pulseFileSource = pulseFileSource;
    }

    public List<ResourceRequirement> getResourceRequirements()
    {
        return resourceRequirements;
    }

    public void setResourceRequirements(List<ResourceRequirement> resourceRequirements)
    {
        this.resourceRequirements = resourceRequirements;
    }

    public ExecutionContext getContext()
    {
        return context;
    }

    public void prepare(String agent)
    {
        bootstrapper.prepare(agent);
        context.addInternalString(BuildProperties.PROPERTY_AGENT, agent);
    }
}
