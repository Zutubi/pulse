package com.zutubi.pulse.prototype.config.project;

import com.zutubi.config.annotations.*;
import com.zutubi.pulse.core.config.AbstractNamedConfiguration;
import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.model.ResourceRequirement;
import com.zutubi.pulse.prototype.config.agent.AgentConfiguration;

import java.util.List;
import java.util.Map;

/**
 *  A build stage is a component of a build that represents the execution of a
 * single recipe on an agent.  Stages execute independently and in parallel
 * where possible, and the build result is the aggregation of all stage
 * results.
 */
@SymbolicName("zutubi.stageConfig")
@Form(fieldOrder = {"name", "recipe", "agent"})
public class BuildStageConfiguration extends AbstractNamedConfiguration
{
    @Reference(optionProvider = "BuildStageAgentOptionProvider")
    private AgentConfiguration agent;
    private String recipe;
    @Ordered
    private Map<String, ResourceProperty> properties;
    private List<ResourceRequirement> requirements;

    public AgentConfiguration getAgent()
    {
        return agent;
    }

    public void setAgent(AgentConfiguration agent)
    {
        this.agent = agent;
    }

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

    @Transient
    public AgentRequirements getAgentRequirements()
    {
        if(agent == null)
        {
            return new AnyCapableAgentRequirements();
        }
        else
        {
            return new SpecificAgentRequirements(agent);
        }
    }
}
