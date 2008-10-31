package com.zutubi.pulse.master.scheduling;

/**
 * <class-comment/>
 */
public enum TriggerState
{
    /**
     * The trigger is scheduled. This means that the trigger will fire when
     * its trigger conditions are met.
     *
     * Using the EventTrigger as an example, a scheduled event trigger will fire
     * when the configured event is generated.
     */
    SCHEDULED,

    /**
     * The trigger is inactive. An inactive trigger will do nothing.
     */
    NONE,

    /**
     * The trigger has been paused. A paused trigger will not fire even if its
     * trigger conditions are met.
     */
    PAUSED
}