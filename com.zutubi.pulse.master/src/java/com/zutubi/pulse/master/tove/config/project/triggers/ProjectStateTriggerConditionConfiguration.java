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