package com.zutubi.pulse.prototype.config.project.triggers;

import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.pulse.scheduling.EventTrigger;
import com.zutubi.pulse.scheduling.ScmChangeEventFilter;
import com.zutubi.pulse.scheduling.Trigger;
import com.zutubi.pulse.scm.ScmChangeEvent;

/**
 *
 *
 */
public class ScmBuildTriggerConfiguration extends TriggerConfiguration
{
    private ConfigurationTemplateManager configurationTemplateManager;

    public Trigger newTrigger()
    {
        ProjectConfiguration project = configurationTemplateManager.getAncestorOfType(this, ProjectConfiguration.class);
        String triggerName = "trigger:" + getHandle();
        String triggerGroup = "project:" + project.getProjectId();

        return new EventTrigger(ScmChangeEvent.class, triggerName, triggerGroup, ScmChangeEventFilter.class);
    }

    public void update(Trigger trigger)
    {
        // no details to be updated.
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}
