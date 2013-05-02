package com.zutubi.pulse.core.postprocessors.api;

/**
 * Available policies for resolving name conflicts.
 */
public enum NameConflictResolution
{
    /**
     * Combine entities with the same name by taking the worst result.
     */
    WORST_RESULT(false),

    /**
     * Combine entities with the same name by taking the best result.
     */
    BEST_RESULT(false),

    /**
     * Combine entities with the same name by taking the first result seen.
     */
    FIRST_RESULT(false),

    /**
     * Combine entities with the same name by taking the last result seen.
     */
    LAST_RESULT(false),

    /**
     * Resolve the conflict by appending an increasing integer to the name
     * until a unique name is found.
     */
    APPEND(true),

    /**
     * Resolve the conflict by prepending an increasing integer to the name
     * until a unique name is found.
     */
    PREPEND(true);

    private boolean uniqueNameGenerated;

    NameConflictResolution(boolean uniqueNameGenerated)
    {
        this.uniqueNameGenerated = uniqueNameGenerated;
    }

    /**
     * @return true iff this type of resolution works by given conflicting cases unique names
     */
    public boolean isUniqueNameGenerated()
    {
        return uniqueNameGenerated;
    }
}
