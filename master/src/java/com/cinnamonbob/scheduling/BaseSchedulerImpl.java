package com.cinnamonbob.scheduling;

/**
 * <class-comment/>
 */
public abstract class BaseSchedulerImpl implements SchedulerImpl
{
    public void trigger(Trigger trigger, Task task)
    {
        TaskExecutionContext context = new TaskExecutionContext();
        trigger(trigger, task, context);
    }

    public void trigger(Trigger trigger, Task task, TaskExecutionContext context)
    {
        context.setTrigger(trigger);
        trigger.trigger();
        task.execute(context);
    }

    public void schedule(Trigger trigger, Task task)
    {
        trigger.setState(TriggerState.ACTIVE);
    }

    public void unschedule(Trigger trigger)
    {
        trigger.setState(TriggerState.NONE);
    }

    public void pause(Trigger trigger)
    {
        trigger.setState(TriggerState.PAUSED);
    }

    public void resume(Trigger trigger)
    {
        trigger.setState(TriggerState.ACTIVE);
    }
}
