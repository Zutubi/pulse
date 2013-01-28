package com.zutubi.pulse.master.upgrade.tasks;

import com.google.common.base.Predicate;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

import java.util.HashMap;
import java.util.Map;

/**
 * A record locator that filters records found by another locator by a given
 * predicate
 */
class PredicateFilterRecordLocator implements RecordLocator
{
    private RecordLocator delegate;
    private Predicate<Record> predicate;

    /**
     * @param delegate  delegate locator used to find records to filter
     * @param predicate defines which records this filter will allow to pass
     */
    public PredicateFilterRecordLocator(RecordLocator delegate, Predicate<Record> predicate)
    {
        this.delegate = delegate;
        this.predicate = predicate;
    }

    public Map<String, Record> locate(RecordManager recordManager)
    {
        Map<String, Record> allRecords = delegate.locate(recordManager);
        Map<String, Record> result = new HashMap<String, Record>();
        for (Map.Entry<String, Record> entry: allRecords.entrySet())
        {
            if (predicate.apply(entry.getValue()))
            {
                result.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }
}