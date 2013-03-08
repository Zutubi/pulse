package com.zutubi.pulse.master.upgrade.tasks;

import com.google.common.collect.ImmutableMap;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

import java.util.Collections;
import java.util.Map;

/**
 * A locator that finds a single record at a specific path, if such a record
 * exists.  If not, no records are returned.
 */
class PathRecordLocator implements RecordLocator
{
    private String path;

    /**
     * @param path path to look up the record from
     * @see com.zutubi.tove.type.record.RecordManager#select(String)
     */
    public PathRecordLocator(String path)
    {
        this.path = path;
    }

    public Map<String, Record> locate(RecordManager recordManager)
    {
        Record record = recordManager.select(path);
        if (record == null)
        {
            return Collections.emptyMap();
        }
        else
        {
            return ImmutableMap.of(path, record);
        }
    }
}
