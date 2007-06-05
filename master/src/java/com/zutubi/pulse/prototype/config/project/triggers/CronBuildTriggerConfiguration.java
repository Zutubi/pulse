package com.zutubi.pulse.prototype.config.project.triggers;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.pulse.scheduling.CronTrigger;
import com.zutubi.pulse.scheduling.Trigger;

/**
 *
 *
 */
@SymbolicName("internal.cronBuildTriggerConfig")
public class CronBuildTriggerConfiguration extends TriggerConfiguration
{
    private ConfigurationPersistenceManager configurationPersistenceManager;

    private String cron;

    public String getCron()
    {
        return cron;
    }

    public void setCron(String cron)
    {
        this.cron = cron;
    }

    public Trigger newTrigger()
    {
        ProjectConfiguration project = configurationPersistenceManager.getAncestorOfType(this, ProjectConfiguration.class);
        String triggerName = "trigger:" + getHandle();
        String triggerGroup = "project:" + project.getProjectId();
        
        return new CronTrigger(cron, triggerName, triggerGroup);
    }

    public void update(Trigger trigger)
    {
        CronTrigger cronTrigger = (CronTrigger) trigger;
        cronTrigger.setCron(cron);
    }

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }
}
