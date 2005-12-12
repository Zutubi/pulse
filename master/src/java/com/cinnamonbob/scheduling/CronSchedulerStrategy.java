package com.cinnamonbob.scheduling;

import org.hibernate.proxy.HibernateProxy;

import java.text.ParseException;

/**
 * <class-comment/>
 */
public class CronSchedulerStrategy extends QuartzSchedulerStrategy
{
    public boolean canHandle(Trigger trigger)
    {
        // its obvious that we can  not rely upon the trigger type to
        // determine what strategy can be applied. Need to come up with
        // a better solution.
        if (HibernateProxy.class.isAssignableFrom(trigger.getClass()))
        {
            trigger = (Trigger) ((HibernateProxy)trigger).getHibernateLazyInitializer().getImplementation();
        }
        return CronTrigger.class.isAssignableFrom(trigger.getClass());
    }

    protected org.quartz.Trigger createTrigger(Trigger trigger) throws SchedulingException
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
