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

package com.zutubi.pulse.master.tove.config.project.triggers;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.trigger.ProjectStateTriggerCondition;
import com.zutubi.pulse.master.trigger.TriggerCondition;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.ItemPicker;
import com.zutubi.tove.annotations.Reference;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.validation.annotations.Required;

import java.util.List;

/**
 * Configuration for {@link com.zutubi.pulse.master.trigger.ProjectStateTriggerCondition}.
 */
@SymbolicName("zutubi.projectStateTriggerConditionConfig")
@Form(fieldOrder = {"project", "state"})
public class ProjectStateTriggerConditionConfiguration extends TriggerConditionConfiguration
{
    @Reference @Required
    private ProjectConfiguration project;
    @ItemPicker(optionProvider = "com.zutubi.pulse.master.tove.config.CompletedResultStateOptionProvider")
    private List<ResultState> states;

    @Override
    public Class<? extends TriggerCondition> conditionType()
    {
        return ProjectStateTriggerCondition.class;
    }

    public ProjectConfiguration getProject()
    {
        return project;
    }

    public void setProject(ProjectConfiguration project)
    {
        this.project = project;
    }

    public List<ResultState> getStates()
    {
        return states;
    }

    public void setStates(List<ResultState> states)
    {
        this.states = states;
    }
}