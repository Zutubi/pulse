package com.zutubi.pulse.prototype.config.project.triggers;

import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.Reference;
import com.zutubi.config.annotations.Select;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.events.build.BuildCompletedEvent;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.pulse.scheduling.BuildCompletedEventFilter;
import com.zutubi.pulse.scheduling.EventTrigger;
import com.zutubi.pulse.scheduling.Trigger;
import com.zutubi.pulse.scheduling.tasks.BuildProjectTask;
import com.zutubi.validation.annotations.Required;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 */
@Form(fieldOrder = { "name", "project", "states"})
@SymbolicName("zutubi.buildCompletedConfig")
public class BuildCompletedTriggerConfiguration extends TriggerConfiguration
{
    @Reference
    @Required
    private ProjectConfiguration project;

    @Select(optionProvider = "com.zutubi.pulse.prototype.CompletedResultStateOptionProvider")
    private List<ResultState> states;

    private ConfigurationProvider configurationProvider;

    public BuildCompletedTriggerConfiguration()
    {
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

    public Trigger newTrigger()
    {
        ProjectConfiguration project = configurationProvider.getAncestorOfType(this, ProjectConfiguration.class);
        EventTrigger trigger = new EventTrigger(BuildCompletedEvent.class, getTriggerName(), getTriggerGroup(project), BuildCompletedEventFilter.class);
        trigger.setTaskClass(BuildProjectTask.class);
        trigger.setProject(project.getProjectId());
        
        Map<Serializable, Serializable> dataMap = trigger.getDataMap();
        dataMap.put(BuildCompletedEventFilter.PARAM_PROJECT, this.project.getProjectId());

        if(states != null && states.size() > 0)
        {
            dataMap.put(BuildCompletedEventFilter.PARAM_STATES, ResultState.getStatesString(states));
        }

        return trigger;
    }

    public void update(Trigger trigger)
    {
        super.update(trigger);

        Map<Serializable, Serializable> dataMap = trigger.getDataMap();
        dataMap.put(BuildCompletedEventFilter.PARAM_PROJECT, this.project.getProjectId());

        if(states != null && states.size() > 0)
        {
            dataMap.put(BuildCompletedEventFilter.PARAM_STATES, ResultState.getStatesString(states));
        }
        else
        {
            dataMap.remove(BuildCompletedEventFilter.PARAM_STATES);
        }
    }

    public String getType()
    {
        return "build completed";
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
