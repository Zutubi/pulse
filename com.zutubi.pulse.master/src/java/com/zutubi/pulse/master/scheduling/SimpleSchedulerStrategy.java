package com.zutubi.pulse.master.scheduling;

import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.TriggerBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * Startegy for scheduling simple triggers, by creating Quartz SimpleTrigger
 * instances.
 */
public class SimpleSchedulerStrategy extends QuartzSchedulerStrategy
{
    public List<String> canHandle()
    {
        return Arrays.asList(SimpleTrigger.TYPE);
    }

    @Override
    protected TriggerBuilder createTriggerBuilder(Trigger trigger) throws SchedulingException
    {
        SimpleTrigger simpleTrigger = (SimpleTrigger) trigger;
        return super.createTriggerBuilder(trigger)
                .startAt(simpleTrigger.getStartTime());
    }

    protected ScheduleBuilder createScheduleBuilder(Trigger trigger) throws SchedulingException
    {
        SimpleTrigger simpleTrigger = (SimpleTrigger) trigger;
        return SimpleScheduleBuilder.simpleSchedule().
                withRepeatCount(simpleTrigger.getRepeatCount()).
                withIntervalInMilliseconds(simpleTrigger.getInterval());
    }
}
