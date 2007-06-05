package com.zutubi.pulse.prototype.config.project.triggers;

import com.zutubi.prototype.config.ConfigurationPersistenceManager;
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
    private ConfigurationPersistenceManager configurationPersistenceManager;

    public Trigger newTrigger()
    {
        ProjectConfiguration project = configurationPersistenceManager.getAncestorOfType(this, ProjectConfiguration.class);
        return new EventTrigger(ScmChangeEvent.class, "trigger:"+getHandle(), "project:" + project.getProjectId(), ScmChangeEventFilter.class);
    }

    public void update(Trigger trigger)
    {

    }

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }
}
