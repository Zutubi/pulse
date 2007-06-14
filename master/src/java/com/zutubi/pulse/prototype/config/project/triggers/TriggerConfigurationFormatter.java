package com.zutubi.pulse.prototype.config.project.triggers;

import com.zutubi.pulse.scheduling.Scheduler;
import com.zutubi.pulse.scheduling.Trigger;

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
            return trigger.getState().toString();
        }
        return "N/A";
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }
}
