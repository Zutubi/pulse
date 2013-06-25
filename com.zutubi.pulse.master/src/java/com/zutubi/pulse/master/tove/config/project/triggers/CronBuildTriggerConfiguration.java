package com.zutubi.pulse.master.tove.config.project.triggers;

import com.zutubi.pulse.master.scheduling.CronTrigger;
import com.zutubi.pulse.master.scheduling.Scheduler;
import com.zutubi.pulse.master.scheduling.SchedulingException;
import com.zutubi.pulse.master.scheduling.Trigger;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wire;
import com.zutubi.util.logging.Logger;
import com.zutubi.validation.annotations.Constraint;
import com.zutubi.validation.annotations.Required;

/**
 * Used to configure a trigger that is defined by a Cron-like expression.
 */
@SymbolicName("zutubi.cronTriggerConfig")
@Form(fieldOrder = {"name", "cron", "disableAfterFiring"})
@Wire
public class CronBuildTriggerConfiguration extends TriggerConfiguration
{
    private static final Logger LOG = Logger.getLogger(CronBuildTriggerConfiguration.class);

    @Required
    @Constraint("CronExpressionValidator")
    private String cron;
    private boolean disableAfterFiring;
    private Scheduler scheduler;

    public String getCron()
    {
        return cron;
    }

    public void setCron(String cron)
    {
        this.cron = cron;
    }

    public boolean isDisableAfterFiring()
    {
        return disableAfterFiring;
    }

    public void setDisableAfterFiring(boolean disableAfterFiring)
    {
        this.disableAfterFiring = disableAfterFiring;
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

    @Override
    public void postFire(Trigger trigger)
    {
        super.postFire(trigger);
        if (disableAfterFiring)
        {
            try
            {
                scheduler.pause(trigger);
            }
            catch (SchedulingException e)
            {
                LOG.severe(e);
            }
        }
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }
}
