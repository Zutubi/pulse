package com.zutubi.pulse.prototype;

import com.zutubi.pulse.prototype.record.Record;
import com.zutubi.pulse.prototype.record.SingleRecord;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class TemplateManagerImpl implements TemplateManager
{
    public TemplateRecord load(Scope scope, String id, String recordName)
    {
        List<OwnedRecord> records = new ArrayList<OwnedRecord>();
        SingleRecord record = new SingleRecord();
        record.put("url", "test url");
        OwnedRecord baseRecord = new OwnedRecord(record, id);
        records.add(baseRecord);
        return new TemplateRecord(records);
    }

    public void store(Record record)
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public void delete(Record record)
    {
        throw new RuntimeException("Method not yet implemented.");
    }
}
