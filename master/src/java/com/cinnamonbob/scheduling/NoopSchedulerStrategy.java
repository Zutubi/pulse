package com.cinnamonbob.scheduling;

/**
 * <class-comment/>
 */
public class NoopSchedulerStrategy implements SchedulerStrategy
{
    public String canHandle()
    {
        return NoopTrigger.TYPE;
    }

    public void pause(Trigger trigger) throws SchedulingException
    {
        trigger.setState(TriggerState.PAUSED);
    }

    public void resume(Trigger trigger) throws SchedulingException
    {
        trigger.setState(TriggerState.ACTIVE);
    }

    public void schedule(Trigger trigger) throws SchedulingException
    {
        trigger.setState(TriggerState.ACTIVE);
    }

    public void unschedule(Trigger trigger) throws SchedulingException
    {
        trigger.setState(TriggerState.NONE);
    }

    public void setTriggerHandler(TriggerHandler handler)
    {

    }
}
