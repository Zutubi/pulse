package com.zutubi.pulse.prototype;

import com.zutubi.pulse.prototype.record.Record;
import com.zutubi.pulse.util.CollectionUtils;
import com.zutubi.pulse.util.Predicate;

import java.util.List;

/**
 * <class comment/>
 */
public class TemplateRecord implements Record
{
    private List<OwnedRecord> templateChain;

    private String symbolicName;

    private String owner;

    public TemplateRecord(List<OwnedRecord> templateChain)
    {
        this.templateChain = templateChain;
    }

    public String get(String name)
    {
        OwnedRecord record = getOwnedRecord(name);
        if (record != null)
        {
            return record.getRecord().get(name);
        }
        return null;
    }

    public String getSymbolicName()
    {
        return symbolicName;
    }

    public String getOwner(final String name)
    {
        OwnedRecord record = getOwnedRecord(name);

        if(record == null)
        {
            record = templateChain.get(templateChain.size());
        }

        return record.getOwner();
    }

    private OwnedRecord getOwnedRecord(final String name)
    {
        return CollectionUtils.find(templateChain, new Predicate<OwnedRecord>()
        {
            public boolean satisfied(OwnedRecord ownedRecord)
            {
                return ownedRecord.getRecord().get(name) != null;
            }
        });
    }

    public String getOwner()
    {
        return owner;
    }
}
