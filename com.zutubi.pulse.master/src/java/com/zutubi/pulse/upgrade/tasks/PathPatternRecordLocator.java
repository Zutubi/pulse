package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

import java.util.Map;

/**
 * A locator that finds all records matching a path pattern.  Path patterns are
 * defined by the {@link com.zutubi.tove.type.record.RecordManager#selectAll(String)}
 * method.
 */
class PathPatternRecordLocator implements RecordLocator
{
    private String pathPattern;

    /**
     * @param pathPattern pattern to use for finding records, may include
     *                    wildcards
     * @see com.zutubi.tove.type.record.RecordManager#selectAll(String)
     */
    public PathPatternRecordLocator(String pathPattern)
    {
        this.pathPattern = pathPattern;
    }

    public Map<String, Record> locate(RecordManager recordManager)
    {
        return recordManager.selectAll(pathPattern);
    }
}
