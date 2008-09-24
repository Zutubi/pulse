package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * A record locator that filters records found by another locator by their type.
 * The type is identified by symbolic name.  Note that subtypes will
 * <strong>not</strong> be found.
 */
class TypeFilterRecordLocator implements RecordLocator
{
    private RecordLocator delegate;
    public String[] acceptableSymbolicNames;

    /**
     * @param delegate                delegate locator used to find records to
     *                                filter
     * @param acceptableSymbolicNames the types that this filter will allow to
     *                                pass
     */
    public TypeFilterRecordLocator(RecordLocator delegate, String... acceptableSymbolicNames)
    {
        this.delegate = delegate;
        this.acceptableSymbolicNames = acceptableSymbolicNames;
    }

    public Map<String, Record> locate(RecordManager recordManager)
    {
        Map<String, Record> allRecords = delegate.locate(recordManager);
        Map<String, Record> result = new HashMap<String, Record>();
        for (Map.Entry<String, Record> entry: allRecords.entrySet())
        {
            if (CollectionUtils.contains(acceptableSymbolicNames, entry.getValue().getSymbolicName()))
            {
                result.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }
}
