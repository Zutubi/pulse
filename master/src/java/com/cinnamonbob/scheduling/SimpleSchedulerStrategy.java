package com.cinnamonbob.scheduling;

/**
 * <class-comment/>
 */
public class SimpleSchedulerStrategy extends QuartzSchedulerStrategy
{
    public boolean canHandle(Trigger trigger)
    {
        return trigger instanceof SimpleTrigger;
    }

    protected org.quartz.Trigger createTrigger(Trigger trigger) throws SchedulingException
    {
        return new org.quartz.SimpleTrigger(trigger.getName(), trigger.getGroup());
    }
}
