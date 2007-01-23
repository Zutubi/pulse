package com.zutubi.pulse.prototype;

import com.zutubi.pulse.prototype.record.Record;
import com.zutubi.pulse.util.CollectionUtils;
import com.zutubi.pulse.util.Predicate;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public Object get(Object name)
    {
        OwnedRecord record = getOwnedRecord((String) name);
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
            record = templateChain.get(templateChain.size() - 1);
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


    public int size()
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public boolean isEmpty()
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public boolean containsKey(Object key)
    {
        return get(key) != null;
    }

    public boolean containsValue(Object value)
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public String put(String key, Object value)
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public String remove(Object key)
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public void putAll(Map<? extends String, ? extends Object> t)
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public void clear()
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public Set<String> keySet()
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public Collection<Object> values()
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public Set<Entry<String, Object>> entrySet()
    {
        throw new RuntimeException("Method not yet implemented.");
    }
}
