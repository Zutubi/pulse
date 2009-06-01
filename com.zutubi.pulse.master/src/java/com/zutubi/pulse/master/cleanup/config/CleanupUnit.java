package com.zutubi.pulse.master.cleanup.config;

/**
 * The CleanupUnit represents the unit for of the cleanup configurations
 * retain field.  
 */
public enum CleanupUnit
{
    /**
     * Indicates that builds can be retained for a specified number of days.
     */
    DAYS,
    
    /**
     * Indicates that builds can be retained for a specified number of builds.
     */
    BUILDS
}
