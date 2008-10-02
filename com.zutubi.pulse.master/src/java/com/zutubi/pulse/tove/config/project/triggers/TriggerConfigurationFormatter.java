package com.zutubi.pulse.tove.config.project.triggers;

import com.zutubi.pulse.master.scheduling.Scheduler;
import com.zutubi.pulse.master.scheduling.Trigger;

/**
 *
 *
 */
public class TriggerConfigurationFormatter
{
    private Scheduler scheduler;

    public String getState(TriggerConfiguration config)
    {
        Trigger trigger = scheduler.getTrigger(config.getTriggerId());
        if (trigger != null)
        {
            return trigger.getState().toString().toLowerCase();
        }
        return "n/a";
    }

    public String getType(TriggerConfiguration config)
    {
        return config.getType();
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }
}
