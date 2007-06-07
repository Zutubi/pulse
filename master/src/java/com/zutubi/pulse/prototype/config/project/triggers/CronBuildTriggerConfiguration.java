package com.zutubi.pulse.prototype.config.project.triggers;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.config.ConfigurationTemplateManager;
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
    private ConfigurationTemplateManager configurationTemplateManager;

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
        ProjectConfiguration project = configurationTemplateManager.getAncestorOfType(this, ProjectConfiguration.class);
        String triggerName = "trigger:" + getHandle();
        String triggerGroup = "project:" + project.getProjectId();
        
        return new CronTrigger(cron, triggerName, triggerGroup);
    }

    public void update(Trigger trigger)
    {
        CronTrigger cronTrigger = (CronTrigger) trigger;
        cronTrigger.setCron(cron);
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}
