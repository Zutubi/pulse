package com.zutubi.pulse.master.tove.config.project.triggers;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.master.scheduling.EventTrigger;
import com.zutubi.pulse.master.scheduling.ScmChangeEventFilter;
import com.zutubi.pulse.master.scheduling.Trigger;
import com.zutubi.pulse.master.scheduling.tasks.BuildProjectTask;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.scm.ScmChangeEvent;
import com.zutubi.tove.config.ConfigurationProvider;

/**
 * A trigger that fires when a code change is detected in the project's SCM.
 */
@SymbolicName("zutubi.scmTriggerConfig")
public class ScmBuildTriggerConfiguration extends TriggerConfiguration
{
    private ConfigurationProvider configurationProvider;

    public Trigger newTrigger()
    {
        ProjectConfiguration project = configurationProvider.getAncestorOfType(this, ProjectConfiguration.class);
        Trigger trigger = new EventTrigger(ScmChangeEvent.class, getTriggerName(), getTriggerGroup(project), ScmChangeEventFilter.class);
        trigger.setTaskClass(BuildProjectTask.class);
        trigger.setProject(project.getProjectId());
        
        return trigger;
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
