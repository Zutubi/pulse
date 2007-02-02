package com.zutubi.pulse.prototype;

import com.zutubi.pulse.prototype.record.Record;
import com.zutubi.pulse.prototype.record.RecordFactory;
import com.zutubi.pulse.prototype.record.SingleRecord;

/**
 * A record factory that creates template records wrapped around actual
 * records.
 */
public class TemplateRecordFactory implements RecordFactory
{
    private String owner;

    public TemplateRecordFactory(String owner)
    {
        this.owner = owner;
    }

    public Record create(String symbolicName)
    {
        return new TemplateRecord(new SingleRecord(symbolicName), owner);
    }
}
