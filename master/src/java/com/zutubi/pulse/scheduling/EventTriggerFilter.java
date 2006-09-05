package com.zutubi.pulse.scheduling;

import com.zutubi.pulse.events.Event;

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
     * @return true iff this filter accepts this event
     */
    boolean accept(Trigger trigger, Event event);

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
