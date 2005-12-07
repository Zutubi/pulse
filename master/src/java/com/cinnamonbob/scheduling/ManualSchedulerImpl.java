package com.cinnamonbob.scheduling;

/**
 * <class-comment/>
 */
public class ManualSchedulerImpl extends BaseSchedulerImpl
{

    public void trigger(Trigger trigger, Task task, TaskExecutionContext context) throws SchedulingException
    {
        if (trigger.getState() == TriggerState.ACTIVE)
        {
            super.trigger(trigger, task, context);
        }
    }

    public void pause(Trigger trigger) throws SchedulingException
    {
        trigger.setState(TriggerState.PAUSED);
    }

    public void resume(Trigger trigger) throws SchedulingException
    {
        trigger.setState(TriggerState.ACTIVE);
    }

    public void schedule(Trigger trigger, Task task) throws SchedulingException
    {
        trigger.setState(TriggerState.ACTIVE);
    }

    public void unschedule(Trigger trigger) throws SchedulingException
    {
        trigger.setState(TriggerState.NONE);
    }
}
