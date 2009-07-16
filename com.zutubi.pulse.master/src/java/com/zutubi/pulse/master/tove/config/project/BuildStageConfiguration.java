package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.core.config.ResourcePropertyConfiguration;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.util.bean.ObjectFactory;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A build stage is a component of a build that represents the execution of a
 * single recipe on an agent.  Stages execute independently and in parallel
 * where possible, and the build result is the aggregation of all stage
 * results.
 */
@SymbolicName("zutubi.stageConfig")
@Form(fieldOrder = {"name", "recipe", "agent"})
@Table(columns = {"name", "recipe"})
@Wire
public class BuildStageConfiguration extends AbstractNamedConfiguration
{
    @Reference(optionProvider = "BuildStageAgentOptionProvider")
    private AgentConfiguration agent;
    @Select(optionProvider = "BuildStageRecipeOptionProvider", editable = true)
    private String recipe;
    @Ordered
    private Map<String, ResourcePropertyConfiguration> properties = new LinkedHashMap<String, ResourcePropertyConfiguration>();
    private List<ResourceRequirementConfiguration> requirements = new LinkedList<ResourceRequirementConfiguration>();

    @Transient
    private ObjectFactory objectFactory;

    public BuildStageConfiguration()
    {
    }

    public BuildStageConfiguration(String name)
    {
        super(name);
    }

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

    public Map<String, ResourcePropertyConfiguration> getProperties()
    {
        return this.properties;
    }

    public void setProperties(Map<String, ResourcePropertyConfiguration> properties)
    {
        this.properties = properties;
    }

    public ResourcePropertyConfiguration getProperty(String name)
    {
        return this.properties.get(name);
    }

    public List<ResourceRequirementConfiguration> getRequirements()
    {
        return requirements;
    }

    public void setRequirements(List<ResourceRequirementConfiguration> requirements)
    {
        this.requirements = requirements;
    }

    @Transient
    public AgentRequirements getAgentRequirements()
    {
        if(agent == null)
        {
            return objectFactory.buildBean(AnyCapableAgentRequirements.class);
        }
        else
        {
            return objectFactory.buildBean(SpecificAgentRequirements.class, new Class[]{ AgentConfiguration.class }, new Object[]{ agent });
        }
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
