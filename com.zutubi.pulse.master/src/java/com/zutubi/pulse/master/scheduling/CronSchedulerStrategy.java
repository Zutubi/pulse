package com.zutubi.pulse.master.scheduling;

import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;

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

    protected ScheduleBuilder createScheduleBuilder(Trigger trigger) throws SchedulingException
    {
        CronTrigger cronTrigger = (CronTrigger) trigger;
        try
        {
            return CronScheduleBuilder.cronSchedule(new CronExpression(cronTrigger.getCron()))
                    .withMisfireHandlingInstructionDoNothing();
        }
        catch (ParseException e)
        {
            throw new SchedulingException(e);
        }
    }
}
