package com.cinnamonbob.scheduling;

/**
 * <class-comment/>
 */
public abstract class BaseSchedulerImpl implements SchedulerImpl
{
    public void trigger(Trigger trigger, Task task) throws SchedulingException
    {
        TaskExecutionContext context = new TaskExecutionContext();
        trigger(trigger, task, context);
    }

    public void trigger(Trigger trigger, Task task, TaskExecutionContext context) throws SchedulingException
    {
        context.setTrigger(trigger);
        trigger.trigger();
        task.execute(context);
    }
}
