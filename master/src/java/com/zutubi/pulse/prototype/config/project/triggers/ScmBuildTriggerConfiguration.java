package com.zutubi.pulse.prototype.config.project.triggers;

import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.pulse.scheduling.EventTrigger;
import com.zutubi.pulse.scheduling.ScmChangeEventFilter;
import com.zutubi.pulse.scheduling.Trigger;
import com.zutubi.pulse.scheduling.tasks.BuildProjectTask;
import com.zutubi.pulse.scm.ScmChangeEvent;
import com.zutubi.config.annotations.SymbolicName;

/**
 *
 *
 */
@SymbolicName("zutubi.scmTriggerConfig")
public class ScmBuildTriggerConfiguration extends TriggerConfiguration
{
    private ConfigurationProvider configurationProvider;

    public Trigger newTrigger()
    {
        ProjectConfiguration project = configurationProvider.getAncestorOfType(this, ProjectConfiguration.class);
        String triggerName = "trigger:" + getHandle();
        String triggerGroup = "project:" + project.getProjectId();

        Trigger trigger = new EventTrigger(ScmChangeEvent.class, triggerName, triggerGroup, ScmChangeEventFilter.class);
        trigger.setTaskClass(BuildProjectTask.class);
        trigger.setProject(project.getHandle());
        
        return trigger;
    }

    public void update(Trigger trigger)
    {
        // no details to be updated.
    }

    public String getType()
    {
        return "scm";
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
