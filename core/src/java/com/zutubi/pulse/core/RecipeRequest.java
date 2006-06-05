package com.zutubi.pulse.core;

import com.zutubi.pulse.core.PulseException;
import com.zutubi.pulse.core.Bootstrapper;
import com.zutubi.pulse.core.Scope;
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

    public RecipeRequest(long id, String recipeName)
    {
        this(id, recipeName, null);
    }

    public RecipeRequest(long id, String recipeName, List<ResourceRequirement> resourceRequirements)
    {
        this(id, null, null, recipeName, resourceRequirements);
    }

    public RecipeRequest(long id, Bootstrapper bootstrapper, String pulseFileSource, String recipeName)
    {
        this(id, bootstrapper, pulseFileSource, recipeName, null);
    }

    public RecipeRequest(long id, Bootstrapper bootstrapper, String pulseFileSource, String recipeName, List<ResourceRequirement> resourceRequirements)
    {
        this.id = id;
        this.bootstrapper = bootstrapper;
        this.pulseFileSource = pulseFileSource;
        this.recipeName = recipeName;
        this.resourceRequirements = resourceRequirements;
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
}
