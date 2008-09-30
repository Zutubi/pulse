package com.zutubi.pulse.tove.config.project.triggers;

import com.zutubi.config.annotations.*;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.events.build.BuildCompletedEvent;
import com.zutubi.pulse.scheduling.BuildCompletedEventFilter;
import com.zutubi.pulse.scheduling.EventTrigger;
import com.zutubi.pulse.scheduling.Trigger;
import com.zutubi.pulse.scheduling.tasks.BuildProjectTask;
import com.zutubi.pulse.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.validation.annotations.Required;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * A trigger that fires when some project build completes, possibly filtered
 * by state.
 */
@Form(fieldOrder = { "name", "project", "states"})
@SymbolicName("zutubi.buildCompletedConfig")
public class BuildCompletedTriggerConfiguration extends TriggerConfiguration
{
    /**
     * The project to listen for builds of.
     */
    @Reference
    @Required
    private ProjectConfiguration project;
    /**
     * If non-empty, the trigger will only fire after builds completed in one
     * of the given states.
     */
    @Select(optionProvider = "com.zutubi.pulse.tove.CompletedResultStateOptionProvider")
    private List<ResultState> states;
    /**
     * If true, the revision of the completed build will also be used for the
     * triggered build.
     */
    @ControllingCheckbox(dependentFields = {"supercedeQueued"})
    private boolean propagateRevision;
    /**
     * If true, build requests raised by this trigger will supercede earlier
     * ones that are already queued (but not commenced).  This prevents
     * several builds with propagated revisions queueing up - instead any
     * existing request is updated to the latest propagated revision.
     */
    private boolean supercedeQueued;

    @Transient
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

    public boolean isPropagateRevision()
    {
        return propagateRevision;
    }

    public void setPropagateRevision(boolean propagateRevision)
    {
        this.propagateRevision = propagateRevision;
    }

    public boolean isSupercedeQueued()
    {
        return supercedeQueued;
    }

    public void setSupercedeQueued(boolean supercedeQueued)
    {
        this.supercedeQueued = supercedeQueued;
    }

    public Trigger newTrigger()
    {
        ProjectConfiguration project = configurationProvider.getAncestorOfType(this, ProjectConfiguration.class);
        EventTrigger trigger = new EventTrigger(BuildCompletedEvent.class, getTriggerName(), getTriggerGroup(project), BuildCompletedEventFilter.class);
        trigger.setTaskClass(BuildProjectTask.class);
        trigger.setProject(project.getProjectId());

        populateDataMap(trigger.getDataMap());
        return trigger;
    }

    public void update(Trigger trigger)
    {
        super.update(trigger);
        populateDataMap(trigger.getDataMap());
    }

    private void populateDataMap(Map<Serializable, Serializable> dataMap)
    {
        dataMap.put(BuildCompletedEventFilter.PARAM_PROJECT, this.project.getProjectId());
        dataMap.put(BuildCompletedEventFilter.PARAM_PROPAGATE_REVISION, propagateRevision);
        dataMap.put(BuildCompletedEventFilter.PARAM_REPLACEABLE, supercedeQueued);

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
