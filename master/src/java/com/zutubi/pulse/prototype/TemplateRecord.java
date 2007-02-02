package com.zutubi.pulse.prototype;

import com.zutubi.pulse.prototype.record.Record;

import java.util.*;

/**
 * <class comment/>
 */
public class TemplateRecord implements Record
{
    private TemplateRecord parent;
    private String owner;
    private Record record;

    public TemplateRecord(Record record, String owner)
    {
        this(null, owner, record);
    }

    public TemplateRecord(TemplateRecord parent, String owner, Record record)
    {
        linkToParent(parent);
        this.owner = owner;
        this.record = record;
    }

    void linkToParent(TemplateRecord parent)
    {
        this.parent = parent;

        if (parent != null)
        {
            // We need to do a deep link: find all child records and link them to
            // their corresponding parent.  We can assume that the layout of
            // fields is identical, but within maps and lists the items in our
            // parent are arbitrary.  It is also guaranteed that every list and
            // map field has a value (i.e. is not null), it just may be an empty
            // list or map.
            for(Entry<String, Object> entry: record.entrySet())
            {
                Object value = entry.getValue();

                // Check for TemplateRecord first as it is also a Map!
                if(value instanceof TemplateRecord)
                {
                    // Subrecords, link to record in the parent of the same name.
                    TemplateRecord parentRecord = (TemplateRecord) parent.get(entry.getKey());
                    if(parentRecord != null)
                    {
                        ((TemplateRecord)value).linkToParent(parentRecord);
                    }
                }
                else if(value instanceof Map)
                {
                    // Link entries of the map that have the same key.
                    linkMapToParent(entry.getKey(), (Map<String, TemplateRecord>) value);
                }
            }
        }
    }

    private void linkMapToParent(String name, Map<String, TemplateRecord> map)
    {
        Map<String, TemplateRecord> parentMap = (Map<String, TemplateRecord>) parent.get(name);
        for(Map.Entry<String, TemplateRecord> entry: map.entrySet())
        {
            TemplateRecord parentRecord = parentMap.get(entry.getKey());
            if(parentRecord != null)
            {
                entry.getValue().linkToParent(parentRecord);
            }
        }
    }

    public Object get(Object key)
    {
        String name = (String) key;
        Object immediateValue = record.get(name);

        // The result will only ever be null for simple values.  These are
        // inherited directly from the parent.
        if(immediateValue == null && parent != null)
        {
            return parent.get(name);
        }
        else if(immediateValue instanceof List)
        {
            return getList(name, (List) immediateValue);
        }
        else if(immediateValue instanceof TemplateRecord)
        {
            // Important to check for TemplateRecord, and before map, as
            // records implement Map.
            return immediateValue;
        }
        else if(immediateValue instanceof Map)
        {
            return getMap(name, (Map<String, TemplateRecord>) immediateValue);
        }
        else
        {
            // Non-null simple value.
            return immediateValue;
        }
    }

    private List getList(String name, List list)
    {
        if(parent == null)
        {
            return list;
        }
        else
        {
            List inherited = new LinkedList((List) parent.get(name));
            inherited.addAll(list);
            return inherited;
        }
    }

    private Map<String, TemplateRecord> getMap(String name, Map<String, TemplateRecord> map)
    {
        if(parent == null)
        {
            return map;
        }
        else
        {
            Map<String, TemplateRecord> inherited = (Map<String, TemplateRecord>) parent.get(name);
            inherited.putAll(map);
            return inherited;
        }
    }

    public String getSymbolicName()
    {
        return record.getSymbolicName();
    }

    public String getOwner()
    {
        return owner;
    }

    public String getFieldOwner(final String name)
    {
        TemplateRecord owner = getOwningRecord(name);
        if(owner == null)
        {
            owner = this;
        }

        return owner.getOwner();
    }

    private TemplateRecord getOwningRecord(final String name)
    {
        if(record.get(name) != null)
        {
            return this;
        }
        else if(parent != null)
        {
            return parent.getOwningRecord(name);
        }
        else
        {
            return null;
        }
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

    public Object put(String key, Object value)
    {
        return record.put(key, value);
    }

    public Object remove(Object key)
    {
        return record.remove(key);
    }

    public void putAll(Map<? extends String, ? extends Object> t)
    {
        record.putAll(t);
    }

    public void clear()
    {
        record.clear();
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
