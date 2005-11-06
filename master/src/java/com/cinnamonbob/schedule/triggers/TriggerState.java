package com.cinnamonbob.schedule.triggers;

/**
 * <class-comment/>
 */
public enum TriggerState
{
    /**
     * Initial state of the trigger.
     */
    NONE,

    /**
     * Active triggers can be triggered.
     */
    ACTIVE,

    /**
     * Paused triggers can not be triggered.
     */
    PAUSED,

    /**
     * Complete triggers will not be triggered.
     */
    COMPLETE
}
