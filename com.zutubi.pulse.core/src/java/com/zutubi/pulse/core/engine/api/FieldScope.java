package com.zutubi.pulse.core.engine.api;

/**
 * Defines available scopes for storing custom fields in a build.
 */
public enum FieldScope
{
    /**
     * The field applies to the build result.
     */
    BUILD,
    /**
     * The field applies to the currently-executing recipe.
     */
    RECIPE
}
