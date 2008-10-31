package com.zutubi.pulse.master.scheduling;

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

    protected org.quartz.Trigger createTrigger(Trigger trigger) throws SchedulingException
    {
        SimpleTrigger simpleTrigger = (SimpleTrigger) trigger;
        return new org.quartz.SimpleTrigger(Long.toString(simpleTrigger.getId()),
                QUARTZ_GROUP,
                simpleTrigger.getStartTime(),
                null,
                simpleTrigger.getRepeatCount(),
                simpleTrigger.getInterval());
    }
}
