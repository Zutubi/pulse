package com.zutubi.pulse.master.tove.config.project.triggers;

import com.zutubi.pulse.master.scheduling.Scheduler;
import com.zutubi.pulse.master.scheduling.Trigger;
import com.zutubi.util.EnumUtils;

/**
 * Format state for trigger instances.
 */
public class TriggerConfigurationStateDisplay
{
    private Scheduler scheduler;

    public String formatState(TriggerConfiguration config)
    {
        Trigger trigger = scheduler.getTrigger(config.getTriggerId());
        if (trigger == null)
        {
            return "[invalid trigger]";
        }
        else
        {
            return EnumUtils.toPrettyString(trigger.getState());
        }
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }
}
