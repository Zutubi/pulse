package com.cinnamonbob.scheduling;

import java.util.Map;
import java.util.HashMap;

/**
 * <class-comment/>
 */
public class TaskExecutionContext
{
    private Map<String, Object> context = new HashMap<String, Object>();

    private Trigger trigger;

    public Object get(String key)
    {
        return context.get(key);
    }

    public void put(String key, Object value)
    {
        context.put(key, value);
    }

    /**
     * Get the trigger that caused this task execution.
     *
     * @return a trigger.
     */
    public Trigger getTrigger()
    {
        return trigger;
    }

    protected void setTrigger(Trigger trigger)
    {
        this.trigger = trigger;
    }
}
