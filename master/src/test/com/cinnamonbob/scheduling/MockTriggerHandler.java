package com.cinnamonbob.scheduling;

/**
 * <class-comment/>
 */
public class MockTriggerHandler implements TriggerHandler
{
    private Task task;

    public void trigger(Trigger trigger) throws SchedulingException
    {
        trigger(trigger, new TaskExecutionContext());
    }

    public void trigger(Trigger trigger, TaskExecutionContext context) throws SchedulingException
    {
        trigger.trigger();
        context.setTrigger(trigger);
        task.execute(context);
    }

    public void setTask(Task task)
    {
        this.task = task;
    }
}
