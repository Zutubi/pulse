package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.util.CollectionUtils;

import java.util.Map;
import java.util.Collections;

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
            return CollectionUtils.asMap(CollectionUtils.asPair(path, record));
        }
    }
}
