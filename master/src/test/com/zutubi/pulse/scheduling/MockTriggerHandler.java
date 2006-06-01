package com.zutubi.pulse.scheduling;

/**
 * <class-comment/>
 */
public class MockTriggerHandler implements TriggerHandler
{
    private Task task;

    public void fire(Trigger trigger) throws SchedulingException
    {
        fire(trigger, new TaskExecutionContext());
    }

    public void fire(Trigger trigger, TaskExecutionContext context) throws SchedulingException
    {
        trigger.fire();
        context.setTrigger(trigger);
        task.execute(context);
    }

    public void setTask(Task task)
    {
        this.task = task;
    }
}
