package com.zutubi.pulse.prototype.config.project.triggers;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Form;
import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.pulse.scheduling.CronTrigger;
import com.zutubi.pulse.scheduling.Trigger;

/**
 *
 *
 */
@SymbolicName("zutubi.cronTriggerConfig")
@Form(fieldOrder = {"name", "cron"})
public class CronBuildTriggerConfiguration extends TriggerConfiguration
{
    private String cron;

    private ConfigurationProvider configurationProvider;

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
        ProjectConfiguration project = configurationProvider.getAncestorOfType(this, ProjectConfiguration.class);
        String triggerName = "trigger:" + getHandle();
        String triggerGroup = "project:" + project.getProjectId();
        
        return new CronTrigger(cron, triggerName, triggerGroup);
    }

    public void update(Trigger trigger)
    {
        CronTrigger cronTrigger = (CronTrigger) trigger;
        cronTrigger.setCron(cron);
    }

    public String getType()
    {
        // TODO: I18N.
        return "cron";
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
