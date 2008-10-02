package com.zutubi.pulse.tove.config.project.triggers;

import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.master.scheduling.CronTrigger;
import com.zutubi.pulse.master.scheduling.Trigger;
import com.zutubi.pulse.master.scheduling.tasks.BuildProjectTask;
import com.zutubi.pulse.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.validation.annotations.Constraint;
import com.zutubi.validation.annotations.Required;

/**
 * Used to configure a trigger that is defined by a Cron-like expression.
 */
@SymbolicName("zutubi.cronTriggerConfig")
@Form(fieldOrder = {"name", "cron"})
public class CronBuildTriggerConfiguration extends TriggerConfiguration
{
    @Required
    @Constraint("CronExpressionValidator")
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
        Trigger trigger =  new CronTrigger(cron, getTriggerName(), getTriggerGroup(project));
        trigger.setTaskClass(BuildProjectTask.class);
        trigger.setProject(project.getProjectId());
        
        return trigger;
    }

    public void update(Trigger trigger)
    {
        super.update(trigger);
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
