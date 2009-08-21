package com.zutubi.pulse.master.scheduling;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

/**
 * <class-comment/>
 */
public class CronSchedulerStrategy extends QuartzSchedulerStrategy
{
    public List<String> canHandle()
    {
        return Arrays.asList(CronTrigger.TYPE);
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
