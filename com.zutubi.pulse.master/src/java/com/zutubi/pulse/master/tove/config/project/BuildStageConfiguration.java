/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.core.resources.api.ResourcePropertyConfiguration;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.Undefined;
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
@Form(fieldOrder = {"name", "recipe", "agent", "terminateBuildOnFailure"})
@Table(columns = {"name", "recipe"})
@Wire
public class BuildStageConfiguration extends AbstractNamedConfiguration
{
    @Reference(optionProvider = "BuildStageAgentOptionProvider")
    private AgentConfiguration agent;
    @Combobox(optionProvider = "BuildStageRecipeOptionProvider", lazy = true)
    private String recipe;
    @Ordered
    private Map<String, ResourcePropertyConfiguration> properties = new LinkedHashMap<String, ResourcePropertyConfiguration>();
    private List<ResourceRequirementConfiguration> requirements = new LinkedList<ResourceRequirementConfiguration>();
    private boolean terminateBuildOnFailure;

    private boolean enabled = true;

    private int priority = Undefined.INTEGER;

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

    public boolean isTerminateBuildOnFailure()
    {
        return terminateBuildOnFailure;
    }

    public void setTerminateBuildOnFailure(boolean terminateBuildOnFailure)
    {
        this.terminateBuildOnFailure = terminateBuildOnFailure;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public boolean hasPriority()
    {
        return priority != Undefined.INTEGER;
    }

    public int getPriority()
    {
        return priority;
    }

    public void setPriority(int priority)
    {
        this.priority = priority;
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
            return objectFactory.buildBean(SpecificAgentRequirements.class, agent);
        }
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
