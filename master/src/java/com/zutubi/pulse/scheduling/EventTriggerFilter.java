/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
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
}
