package com.cinnamonbob.scheduling;

/**
 * <class-comment/>
 */
public class DefaultTriggerHandler implements TriggerHandler
{
    public void trigger(Trigger trigger) throws SchedulingException
    {
        TaskExecutionContext context = new TaskExecutionContext();
        trigger(trigger, context);
    }

    public void trigger(Trigger trigger, TaskExecutionContext context) throws SchedulingException
    {
        context.setTrigger(trigger);
        trigger.trigger();
        // determine the task to be executed.
        try
        {
            Task task = trigger.getTaskClass().newInstance();
            task.execute(context);
        }
        catch (Exception e)
        {
            throw new SchedulingException(e);
        }
    }
}
