package com.zutubi.pulse.core;

import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.model.ResourceRequirement;

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
    private String pulseFileSource;
    /**
     * Context for the recipe.
     */
    private ExecutionContext context;

    private List<ResourceRequirement> resourceRequirements = new LinkedList<ResourceRequirement>();

    private List<ResourceProperty> properties = new LinkedList<ResourceProperty>();

    public RecipeRequest(ExecutionContext context)
    {
        this(null, null, context);
    }

    public RecipeRequest(Bootstrapper bootstrapper, String pulseFileSource, ExecutionContext context)
    {
        this.bootstrapper = bootstrapper;
        this.pulseFileSource = pulseFileSource;
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

    public ExecutionContext getContext()
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

    public void prepare(String agent)
    {
        bootstrapper.prepare(agent);
        context.addInternalString(BuildProperties.PROPERTY_AGENT, agent);
    }
}
