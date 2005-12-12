package com.cinnamonbob.scheduling;

import org.hibernate.proxy.HibernateProxy;

/**
 * <class-comment/>
 */
public class SimpleSchedulerStrategy extends QuartzSchedulerStrategy
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
        return SimpleTrigger.class.isAssignableFrom(trigger.getClass());
    }

    protected org.quartz.Trigger createTrigger(Trigger trigger) throws SchedulingException
    {
        return new org.quartz.SimpleTrigger(trigger.getName(), trigger.getGroup());
    }
}
