package com.zutubi.pulse.core;

import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.model.ResourceRequirement;

import java.util.List;

/**
 * A request to execute a specific recipe.  Includes details about how to
 * bootstrap this step of the build (e.g. by SCM checkout, or by using a
 * working directory left by a previous recipe).
 */
public class RecipeRequest
{
    private String project;
    private boolean incremental;
    /**
     * The unique identifier for the execution of this recipe.
     */
    private long id;
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
     * The name of the recipe to execute, or null to execute the default.
     */
    private String recipeName;
    /**
     * Required resources for the build.  If the pulse file is set lazily,
     * some requirements may be added at that time.
     */
    private List<ResourceRequirement> resourceRequirements;
    /**
     * Properties to import into the global scope.
     */
    private List<ResourceProperty> properties;

    public RecipeRequest(String project, long id, String recipeName, boolean incremental)
    {
        this(project, id, recipeName, incremental, null, null);
    }

    public RecipeRequest(String project, long id, String recipeName, boolean incremental, List<ResourceRequirement> resourceRequirements, List<ResourceProperty> properties)
    {
        this(project, id, null, null, recipeName, incremental, resourceRequirements, properties);
    }

    public RecipeRequest(long id, Bootstrapper bootstrapper, String pulseFileSource, String recipeName)
    {
        this(null, id, bootstrapper, pulseFileSource, recipeName, false, null, null);
    }

    public RecipeRequest(String project, long id, Bootstrapper bootstrapper, String pulseFileSource, String recipeName, boolean incremental)
    {
        this(project, id, bootstrapper, pulseFileSource, recipeName, incremental, null, null);
    }

    public RecipeRequest(String project, long id, Bootstrapper bootstrapper, String pulseFileSource, String recipeName, boolean incremental, List<ResourceRequirement> resourceRequirements, List<ResourceProperty> properties)
    {
        this.project = project;
        this.id = id;
        this.bootstrapper = bootstrapper;
        this.pulseFileSource = pulseFileSource;
        this.recipeName = recipeName;
        this.incremental = incremental;
        this.resourceRequirements = resourceRequirements;
        this.properties = properties;
    }

    public String getProject()
    {
        return project;
    }

    public boolean isIncremental()
    {
        return incremental;
    }

    public long getId()
    {
        return id;
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
        return recipeName;
    }

    public String getRecipeNameSafe()
    {
        if(recipeName == null)
        {
            return "[default]";
        }
        else
        {
            return recipeName;
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

    public List<ResourceProperty> getProperties()
    {
        return properties;
    }

    public void setProperties(List<ResourceProperty> properties)
    {
        this.properties = properties;
    }

    public void prepare(String agent)
    {
        bootstrapper.prepare(agent);
        properties.add(new ResourceProperty("agent", agent));
        properties.add(new ResourceProperty("recipe", recipeName == null ? "[default]" : recipeName));
    }
}
