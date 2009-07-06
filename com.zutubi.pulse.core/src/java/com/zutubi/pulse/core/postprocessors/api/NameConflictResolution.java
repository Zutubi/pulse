package com.zutubi.pulse.core.postprocessors.api;

/**
 * Available policies for resolving name conflicts.
 */
public enum NameConflictResolution
{
    /**
     * Resolve the conflict by appending an increasing integer to the name
     * until a unique name is found.
     */
    APPEND,

    /**
     * Do not resolve conflicts, combine entities with the same name.
     */
    OFF,
    
    /**
     * Resolve the conflict by prepending an increasing integer to the name
     * until a unique name is found.
     */
    PREPEND
}
