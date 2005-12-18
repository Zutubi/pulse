package com.cinnamonbob.scheduling;

/**
 * <class-comment/>
 */
public class SimpleSchedulerStrategy extends QuartzSchedulerStrategy
{
    public String canHandle()
    {
        return SimpleTrigger.TYPE;
    }

    protected org.quartz.Trigger createTrigger(Trigger trigger) throws SchedulingException
    {
        SimpleTrigger simpleTrigger = (SimpleTrigger) trigger;
        return new org.quartz.SimpleTrigger(simpleTrigger.getName(),
                simpleTrigger.getGroup(),
                simpleTrigger.getRepeatCount(),
                simpleTrigger.getInterval());
    }
}
