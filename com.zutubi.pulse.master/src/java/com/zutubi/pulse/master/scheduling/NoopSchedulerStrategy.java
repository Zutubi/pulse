package com.zutubi.pulse.master.scheduling;

import java.util.Arrays;
import java.util.List;

/**
 * <class-comment/>
 */
public class NoopSchedulerStrategy implements SchedulerStrategy
{
    public List<String> canHandle()
    {
        return Arrays.asList(NoopTrigger.TYPE);
    }

    public void init(Trigger trigger) throws SchedulingException
    {
    }

    public void pause(Trigger trigger) throws SchedulingException
    {
        trigger.setState(TriggerState.PAUSED);
    }

    public void resume(Trigger trigger) throws SchedulingException
    {
        trigger.setState(TriggerState.SCHEDULED);
    }

    public void stop(boolean force)
    {
    }

    public void schedule(Trigger trigger) throws SchedulingException
    {
        trigger.setState(TriggerState.SCHEDULED);
    }

    public void unschedule(Trigger trigger) throws SchedulingException
    {
        trigger.setState(TriggerState.NONE);
    }

    public void setTriggerHandler(TriggerHandler handler)
    {

    }

    public boolean dependsOnProject(Trigger trigger, long projectId)
    {
        return false;
    }
}
