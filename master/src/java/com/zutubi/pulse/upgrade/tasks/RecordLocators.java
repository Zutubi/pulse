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

    /**
     * Create a new locator that can filter the output of another locator based
     * on the symbolic names of the records.  Only records with symbolic names
     * that are acceptable will be allowed to pass.
     *
     * @param delegate                delegate locator used to find records to
     *                                filter
     * @param acceptableSymbolicNames the types that this filter will allow to
     *                                pass
     * @return a type-filtering locater
     */
    public static RecordLocator newTypeFilter(RecordLocator delegate, String... acceptableSymbolicNames)
    {
        return new TypeFilterRecordLocator(delegate, acceptableSymbolicNames);
    }
}
