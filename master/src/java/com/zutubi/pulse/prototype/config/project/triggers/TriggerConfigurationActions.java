package com.zutubi.pulse.prototype.config.project.triggers;

import com.zutubi.pulse.scheduling.Scheduler;
import com.zutubi.pulse.scheduling.Trigger;
import com.zutubi.pulse.scheduling.SchedulingException;

/**
 *
 *
 */
public class TriggerConfigurationActions
{
    private Scheduler scheduler;

    public void doPause(TriggerConfiguration config) throws SchedulingException
    {
        Trigger trigger = scheduler.getTrigger(config.getTriggerId());
        if (!trigger.isPaused())
        {
            scheduler.pause(trigger);
        }
    }

    public boolean isPauseEnabled(TriggerConfiguration config)
    {
        Trigger trigger = scheduler.getTrigger(config.getTriggerId());
        return !trigger.isPaused();
    }

    public void doResume(TriggerConfiguration config) throws SchedulingException
    {
        Trigger trigger = scheduler.getTrigger(config.getTriggerId());
        if (trigger.isPaused())
        {
            scheduler.resume(trigger);
        }
    }

    public boolean isResumeEnabled(TriggerConfiguration config)
    {
        Trigger trigger = scheduler.getTrigger(config.getTriggerId());
        return trigger.isPaused();
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }
}
