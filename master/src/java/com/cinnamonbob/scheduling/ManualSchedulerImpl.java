package com.cinnamonbob.scheduling;

/**
 * <class-comment/>
 */
public class ManualSchedulerImpl extends BaseSchedulerImpl
{

    public void trigger(Trigger trigger, Task task, TaskExecutionContext context)
    {
        if (trigger.getState() == TriggerState.ACTIVE)
        {
            super.trigger(trigger, task, context);
        }
    }
}
