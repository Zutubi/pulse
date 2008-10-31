package com.zutubi.pulse.master.tove.config.project.triggers;

import com.zutubi.pulse.master.scheduling.Scheduler;
import com.zutubi.pulse.master.scheduling.SchedulingException;
import com.zutubi.pulse.master.scheduling.Trigger;

import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class TriggerConfigurationActions
{
    private Scheduler scheduler;

    public List<String> getActions(TriggerConfiguration config)
    {
        List<String> actions = new LinkedList<String>();
        Trigger trigger = scheduler.getTrigger(config.getTriggerId());
        if (trigger != null && trigger.isScheduled())
        {
            if (trigger.isPaused())
            {
                actions.add("resume");
            }
            else
            {
                actions.add("pause");
            }
        }
        return actions;
    }

    public void doPause(TriggerConfiguration config) throws SchedulingException
    {
        Trigger trigger = scheduler.getTrigger(config.getTriggerId());
        if (!trigger.isPaused())
        {
            scheduler.pause(trigger);
        }
    }

    public void doResume(TriggerConfiguration config) throws SchedulingException
    {
        Trigger trigger = scheduler.getTrigger(config.getTriggerId());
        if (trigger.isPaused())
        {
            scheduler.resume(trigger);
        }
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }
}
