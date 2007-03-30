package com.zutubi.prototype.type.record;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Simple record that holds key:value data, along with meta data.
 */
public class MutableRecordImpl implements Record
{
    private Map<String, String> meta = new HashMap<String, String>();

    private Map<String, Object> data = new HashMap<String, Object>();

    public MutableRecordImpl()
    {
    }

    public void setSymbolicName(String name)
    {
        meta.put("symbolicName", name);
    }

    public String getSymbolicName()
    {
        return meta.get("symbolicName");
    }

    public void putMeta(String key, String value)
    {
        meta.put(key, value);
    }

    public String getMeta(String key)
    {
        return meta.get(key);
    }

    public void putAll(MutableRecordImpl newRecord)
    {
        meta.putAll(newRecord.meta);
        data.putAll(newRecord.data);
    }

    public Object put(String key, Object value)
    {
        return data.put(key, value);
    }

    public int size()
    {
        return data.size();
    }

    public Set<String> keySet()
    {
        return data.keySet();
    }

    public boolean isEmpty()
    {
        return data.isEmpty();
    }

    public boolean containsKey(Object key)
    {
        return data.containsKey(key);
    }

    public boolean containsValue(Object value)
    {
        return data.containsValue(value);
    }

    public Object get(Object key)
    {
        return data.get(key);
    }

    public Object remove(Object key)
    {
        return data.remove(key);
    }

    public void putAll(Record t)
    {
        // todo: fix.
        this.data.putAll(((MutableRecordImpl)t).data);
    }

    public void clear()
    {
        meta.clear();
        data.clear();
    }

    public Collection<Object> values()
    {
        return data.values();
    }

    public Set<Map.Entry<String, Object>> entrySet()
    {
        return data.entrySet();
    }

    public MutableRecordImpl clone() throws CloneNotSupportedException
    {
        super.clone();

        MutableRecordImpl clone = new MutableRecordImpl();

        for (Map.Entry<String, String> entry : meta.entrySet())
        {
            String key = entry.getKey();
            String value = entry.getValue();
            clone.putMeta(key, value);
        }

        for (Map.Entry<String, Object> entry : data.entrySet())
        {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Record)
            {
                value = ((Record) value).clone();
            }
            clone.put(key, value);
        }
        return clone;
    }

    public void update(MutableRecordImpl record)
    {
        Map<String, Object> newData = new HashMap<String, Object>();

        for (Map.Entry<String, Object> entry : data.entrySet())
        {
            if (entry.getValue() instanceof Record)
            {
                newData.put(entry.getKey(), entry.getValue());
            }
        }

        newData.putAll(record.data);

        data.clear();
        data.putAll(newData);

        meta.clear();
        meta.putAll(record.meta);
    }
}
