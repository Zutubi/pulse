/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.scheduling;

import java.util.Map;
import java.util.HashMap;

/**
 * The TaskExecutionContext stored context information relating to a tasks execution
 * environment.
 * 
 * It contains a reference to the Trigger that triggered the tasks execution.
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
