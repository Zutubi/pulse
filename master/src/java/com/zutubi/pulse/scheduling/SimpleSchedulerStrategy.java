package com.zutubi.pulse.scheduling;

import java.util.List;
import java.util.Arrays;

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
