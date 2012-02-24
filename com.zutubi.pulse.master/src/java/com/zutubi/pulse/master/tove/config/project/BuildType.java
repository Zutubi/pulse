package com.zutubi.pulse.master.tove.config.project;

/**
 * Defines where build stage recipes are run.
 */
public enum BuildType
{
    /**
     * Run the recipe in the temporary recipe directory.
     */
    CLEAN_BUILD,
    /**
     * Run the recipe in the persistent project work directory.
     */
    INCREMENTAL_BUILD,
}
