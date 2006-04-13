/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.scheduling;

/**
 * <class-comment/>
 */
public class TestTriggerHandler implements TriggerHandler
{
    private long triggerCount;
    private boolean triggered;

    public void fire(Trigger trigger) throws SchedulingException
    {
        fire(trigger, new TaskExecutionContext());
    }

    public void fire(Trigger trigger, TaskExecutionContext context) throws SchedulingException
    {
        trigger.fire();
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
