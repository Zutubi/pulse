package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

import java.util.HashMap;
import java.util.Map;

/**
 * A record locator that filters records found by another locator, only passing
 * records that are not inherited.  That is, records that are the first
 * definition of a path in a hierarchy.
 */
class FirstDefinedFilterRecordLocator implements RecordLocator
{
    private RecordLocator delegate;
    private TemplatedScopeDetails scope;

    /**
     * @param delegate  delegate locator used to find records to filter
     * @param scope     the scope in which the delegate is locating records
     */
    public FirstDefinedFilterRecordLocator(RecordLocator delegate, TemplatedScopeDetails scope)
    {
        this.delegate = delegate;
        this.scope = scope;
    }

    public Map<String, Record> locate(RecordManager recordManager)
    {
        Map<String, Record> allRecords = delegate.locate(recordManager);
        Map<String, Record> result = new HashMap<String, Record>();
        for (Map.Entry<String, Record> entry: allRecords.entrySet())
        {
            if (!scope.hasAncestor(entry.getKey()))
            {
                result.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }
}