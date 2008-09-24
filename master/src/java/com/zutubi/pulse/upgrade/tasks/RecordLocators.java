package com.zutubi.pulse.upgrade.tasks;

/**
 * Static factory methods for creating {@link RecordLocator} instances.
 */
public class RecordLocators
{
    /**
     * Create a new locator that finds records by their path pattern.  Patterns
     * are defined by {@link com.zutubi.tove.type.record.RecordManager#selectAll(String)}.
     *
     * @param pathPattern path pattern used for location, may contain wildcards
     * @return a locator which will find all records matching the given pattern
     */
    public static RecordLocator newPathPattern(String pathPattern)
    {
        return new PathPatternRecordLocator(pathPattern);
    }
}
