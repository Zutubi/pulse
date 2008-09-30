package com.zutubi.pulse.scheduling;

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

    /**
     * Returns true if the trigger is based upon the given project.  This
     * does not include triggers for the project itself.
     *
     * @param trigger the trigger to test
     * @param projectId id of the project to test
     * @return true iff the given trigger depends on the given project
     */
    boolean dependsOnProject(Trigger trigger, long projectId);
}
