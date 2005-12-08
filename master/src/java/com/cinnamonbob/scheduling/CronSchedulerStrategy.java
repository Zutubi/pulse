package com.cinnamonbob.scheduling;

import java.text.ParseException;

/**
 * <class-comment/>
 */
public class CronSchedulerStrategy extends QuartzSchedulerStrategy
{
    public boolean canHandle(Trigger trigger)
    {
        return trigger instanceof CronTrigger;
    }

    protected org.quartz.Trigger createTrigger(Trigger trigger, Task task) throws SchedulingException
    {
        CronTrigger cronTrigger = (CronTrigger) trigger;
        try
        {
            return new org.quartz.CronTrigger(cronTrigger.getName(), cronTrigger.getGroup(), cronTrigger.getCron());
        }
        catch (ParseException e)
        {
            throw new SchedulingException(e);
        }
    }
}
