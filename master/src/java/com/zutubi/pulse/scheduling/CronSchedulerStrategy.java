package com.zutubi.pulse.scheduling;

import java.text.ParseException;

/**
 * <class-comment/>
 */
public class CronSchedulerStrategy extends QuartzSchedulerStrategy
{
    public String canHandle()
    {
        return CronTrigger.TYPE;
    }

    public boolean dependsOnProject(Trigger trigger, long projectId)
    {
        return false;
    }

    protected org.quartz.Trigger createTrigger(Trigger trigger) throws SchedulingException
    {
        CronTrigger cronTrigger = (CronTrigger) trigger;
        try
        {
            org.quartz.CronTrigger quartzTrigger = new org.quartz.CronTrigger(Long.toString(cronTrigger.getId()), QUARTZ_GROUP, cronTrigger.getCron());
            quartzTrigger.setMisfireInstruction(org.quartz.CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);
            return quartzTrigger;
        }
        catch (ParseException e)
        {
            throw new SchedulingException(e);
        }
    }
}
