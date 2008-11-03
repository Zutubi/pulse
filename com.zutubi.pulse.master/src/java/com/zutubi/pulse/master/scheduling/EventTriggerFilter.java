package com.zutubi.pulse.master.scheduling;

import com.zutubi.events.Event;

/**
 * Allows event triggers to filter incoming events based on data in the
 * trigger and/or event.
 */
public interface EventTriggerFilter
{
    /**
     * Filters acceptable events based on the trigger and event instances.
     *
     * @param trigger the trigger that is checking the event
     * @param event   the event to check
     * @param context the context in which the trigger task will execute if
     *                the event is accepted - filters may populate the
     *                context to pass information to the task
     * @return true iff this filter accepts this event
     */
    boolean accept(Trigger trigger, Event event, TaskExecutionContext context);
}
