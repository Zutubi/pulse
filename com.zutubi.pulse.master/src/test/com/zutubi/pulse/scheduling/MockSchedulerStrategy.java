package com.zutubi.pulse.scheduling;

import java.util.List;
import java.util.Arrays;

/**
 * <class-comment/>
 */
public class MockSchedulerStrategy implements SchedulerStrategy
{
    public List<String> canHandle()
    {
        return Arrays.asList(CronTrigger.TYPE,SimpleTrigger.TYPE,EventTrigger.TYPE,OneShotTrigger.TYPE,NoopTrigger.TYPE );
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
