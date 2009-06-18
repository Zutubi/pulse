package com.zutubi.pulse.master.tove.config.project.triggers;

import com.zutubi.pulse.master.DependentBuildEventFilter;
import com.zutubi.pulse.master.events.build.BuildCompletedEvent;
import com.zutubi.pulse.master.scheduling.EventTrigger;
import com.zutubi.pulse.master.scheduling.Trigger;
import com.zutubi.pulse.master.scheduling.tasks.BuildProjectTask;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.ConfigurationProvider;

import java.io.Serializable;
import java.util.Map;

/**
 * The trigger configuration for dependent build triggers.
 */
@Form(fieldOrder = { "name", "propagateStatus", "propagateVersion" })
@SymbolicName("zutubi.dependentBuildTriggerConfig")
public class DependentBuildTriggerConfiguration extends TriggerConfiguration
{
    private ConfigurationProvider configurationProvider;

    /**
     * If true, build requests raised by this trigger will inherit the status
     * of the completed build.
     */
    private boolean propagateStatus;

    /**
     * If true, build requests raised by this trigger will inherit the version
     * of the completed build.
     */
    private boolean propagateVersion;

    public Trigger newTrigger()
    {
        // configured project.
        ProjectConfiguration project = configurationProvider.getAncestorOfType(this, ProjectConfiguration.class);

        EventTrigger trigger = new EventTrigger(BuildCompletedEvent.class, getTriggerName(), getTriggerGroup(project), DependentBuildEventFilter.class);
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
        dataMap.put(DependentBuildEventFilter.PARAM_PROPAGATE_STATUS, propagateStatus);
        dataMap.put(DependentBuildEventFilter.PARAM_PROPAGATE_VERSION, propagateVersion);
    }

    public boolean isPropagateStatus()
    {
        return propagateStatus;
    }

    public void setPropagateStatus(boolean propagateStatus)
    {
        this.propagateStatus = propagateStatus;
    }

    public boolean isPropagateVersion()
    {
        return propagateVersion;
    }

    public void setPropagateVersion(boolean propagateVersion)
    {
        this.propagateVersion = propagateVersion;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
