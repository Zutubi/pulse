package com.cinnamonbob.scheduling;

import org.quartz.*;

/**
 * <class-comment/>
 */
public class SimpleSchedulerStrategy extends QuartzSchedulerStrategy
{
    public boolean canHandle(Trigger trigger)
    {
        return trigger instanceof SimpleTrigger;
    }

    protected org.quartz.Trigger createTrigger(Trigger trigger, Task task) throws SchedulingException
    {
        return new org.quartz.SimpleTrigger(trigger.getName(), trigger.getGroup());
    }
}
