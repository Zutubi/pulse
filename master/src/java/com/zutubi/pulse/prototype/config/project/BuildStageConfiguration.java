package com.zutubi.pulse.prototype.config.project;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Transient;
import com.zutubi.pulse.core.config.AbstractNamedConfiguration;
import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.model.AnyCapableBuildHostRequirements;
import com.zutubi.pulse.model.BuildHostRequirements;
import com.zutubi.pulse.model.ResourceRequirement;

import java.util.List;
import java.util.Map;

/**
 *
 *
 */
@SymbolicName("internal.stageConfig")
public class BuildStageConfiguration extends AbstractNamedConfiguration
{
    @Transient
    private BuildHostRequirements hostRequirements = new AnyCapableBuildHostRequirements();
    
    private String recipe;

    private Map<String, ResourceProperty> properties;

    private List<ResourceRequirement> requirements;

    public String getRecipe()
    {
        return this.recipe;
    }

    public void setRecipe(String recipe)
    {
        this.recipe = recipe;
    }

    public Map<String, ResourceProperty> getProperties()
    {
        return this.properties;
    }

    public void setProperties(Map<String, ResourceProperty> properties)
    {
        this.properties = properties;
    }

    public ResourceProperty getProperty(String name)
    {
        return this.properties.get(name);
    }

    public List<ResourceRequirement> getRequirements()
    {
        return requirements;
    }

    public void setRequirements(List<ResourceRequirement> requirements)
    {
        this.requirements = requirements;
    }

    public BuildHostRequirements getHostRequirements()
    {
        return hostRequirements;
    }

    public void setHostRequirements(BuildHostRequirements hostRequirements)
    {
        this.hostRequirements = hostRequirements;
    }
}
