package com.zutubi.pulse.master.tove.config.project.triggers;

import com.zutubi.pulse.master.scheduling.CronTrigger;
import com.zutubi.pulse.master.scheduling.Trigger;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.validation.annotations.Constraint;

/**
 * Used to configure a trigger that is defined by a Cron-like expression.
 */
@SymbolicName("zutubi.cronTriggerConfig")
@Form(fieldOrder = {"name", "cron"})
public class CronBuildTriggerConfiguration extends TriggerConfiguration
{
    @Constraint("CronExpressionValidator")
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
        return new CronTrigger(cron, getName());
    }

    public void update(Trigger trigger)
    {
        super.update(trigger);
        CronTrigger cronTrigger = (CronTrigger) trigger;
        cronTrigger.setCron(cron);
    }
}
