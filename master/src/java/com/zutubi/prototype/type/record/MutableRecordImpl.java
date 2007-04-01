package com.zutubi.prototype.type.record;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Simple record that holds key:value data, along with meta data.
 */
public class MutableRecordImpl implements MutableRecord
{
    private Map<String, String> meta = new HashMap<String, String>();

    private Map<String, Object> data = new HashMap<String, Object>();

    private static final String SYMBOLIC_NAME = "symbolicName";

    public void setSymbolicName(String name)
    {
        meta.put(SYMBOLIC_NAME, name);
    }

    public String getSymbolicName()
    {
        return meta.get(SYMBOLIC_NAME);
    }

    public void putMeta(String key, String value)
    {
        meta.put(key, value);
    }

    public String getMeta(String key)
    {
        return meta.get(key);
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

    public Set<String> metaKeySet()
    {
        return meta.keySet();
    }

    public boolean containsKey(Object key)
    {
        return data.containsKey(key);
    }

    public Object get(Object key)
    {
        return data.get(key);
    }

    public Object remove(Object key)
    {
        return data.remove(key);
    }

    public void clear()
    {
        meta.clear();
        data.clear();
    }

    public MutableRecord createMutable()
    {
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
                value = ((Record) value).createMutable();
            }
            clone.put(key, value);
        }
        return clone;
    }

    public void update(Record record)
    {
        Map<String, Object> newData = new HashMap<String, Object>();

        // take the existing records but not the existing primitives.
        for (Map.Entry<String, Object> entry : data.entrySet())
        {
            if (entry.getValue() instanceof Record)
            {
                newData.put(entry.getKey(), entry.getValue());
            }
        }

        // take the new data from the record.
        for (String key : record.keySet())
        {
            newData.put(key, record.get(key));
        }

        data.clear();
        data.putAll(newData);

        // copy across all of the meta data.
        Map<String, String> newMetaData = new HashMap<String, String>();
        for (String key: record.metaKeySet())
        {
            newMetaData.put(key, record.getMeta(key));
        }
        
        meta.clear();
        meta.putAll(newMetaData);
    }
}
