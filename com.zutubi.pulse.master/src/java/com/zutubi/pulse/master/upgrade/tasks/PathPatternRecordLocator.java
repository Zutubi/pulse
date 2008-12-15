package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

import java.util.Map;
import java.util.HashMap;

/**
 * A locator that finds all records matching a path pattern.  Path patterns are
 * defined by the {@link com.zutubi.tove.type.record.RecordManager#selectAll(String)}
 * method.
 */
class PathPatternRecordLocator implements RecordLocator
{
    private String[] pathPatterns;

    /**
     * @param pathPatterns patterns to use for finding records, may include
     *                    wildcards
     * @see com.zutubi.tove.type.record.RecordManager#selectAll(String)
     */
    public PathPatternRecordLocator(String... pathPatterns)
    {
        this.pathPatterns = pathPatterns;
    }

    public Map<String, Record> locate(RecordManager recordManager)
    {
        Map<String, Record> result = new HashMap<String, Record>();
        for (String pathPattern : pathPatterns)
        {
            result.putAll(recordManager.selectAll(pathPattern));
        }
        return result;
    }
}
