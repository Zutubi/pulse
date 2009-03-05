package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

import java.util.HashMap;
import java.util.Map;

/**
 * A locator that returns the union of all records returned by delegate
 * locators.
 */
public class UnionRecordLocator implements RecordLocator
{
    private RecordLocator[] delegates;

    public UnionRecordLocator(RecordLocator... delegates)
    {
        this.delegates = delegates;
    }

    public Map<String, Record> locate(RecordManager recordManager)
    {
        Map<String, Record> result = new HashMap<String, Record>();
        for (RecordLocator delegate: delegates)
        {
            result.putAll(delegate.locate(recordManager));
        }
        return result;
    }
}
