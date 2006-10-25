package com.zutubi.pulse.scheduling;

/**
 * <class-comment/>
 */
public class NoopSchedulerStrategy implements SchedulerStrategy
{
    public String canHandle()
    {
        return NoopTrigger.TYPE;
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
