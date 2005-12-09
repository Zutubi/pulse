package com.cinnamonbob.scheduling;

/**
 * <class-comment/>
 */
public class TestTriggerHandler implements TriggerHandler
{
    private long triggerCount;
    private boolean triggered;

    public void trigger(Trigger trigger) throws SchedulingException
    {
        trigger(trigger, new TaskExecutionContext());
    }

    public void trigger(Trigger trigger, TaskExecutionContext context) throws SchedulingException
    {
        trigger.trigger();
        triggerCount++;
        triggered = true;
    }

    public boolean wasTriggered()
    {
        return triggered;
    }

    public long getTriggerCount()
    {
        return triggerCount;
    }

    public void reset()
    {
        triggerCount = 0;
        triggered = false;
    }
}
