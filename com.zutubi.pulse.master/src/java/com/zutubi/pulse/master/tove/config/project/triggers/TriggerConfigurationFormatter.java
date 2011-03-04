package com.zutubi.pulse.master.tove.config.project.triggers;

import com.zutubi.pulse.master.scheduling.Scheduler;
import com.zutubi.pulse.master.scheduling.Trigger;

/**
 * Formats display fields for triggers.
 */
public class TriggerConfigurationFormatter
{
    private Scheduler scheduler;

    public String getState(TriggerConfiguration config)
    {
        long triggerId = config.getTriggerId();
        if (triggerId != 0)
        {
            Trigger trigger = scheduler.getTrigger(triggerId);
            if (trigger != null)
            {
                return trigger.getState().toString().toLowerCase();
            }
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
