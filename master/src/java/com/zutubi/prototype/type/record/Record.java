package com.zutubi.prototype.type.record;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Collection;

/**
 *
 *
 */
public class Record implements Map<String, Object>
{
    private Map<String, String> meta = new HashMap<String, String>();
    
    private Map<String, Object> data = new HashMap<String, Object>();

    public Record()
    {
    }

    public void putMetaProperty(String key, String value)
    {
        meta.put(key, value);
    }

    public String getMetaProperty(String key)
    {
        return meta.get(key);
    }

    public void putAll(Record newRecord)
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

    public void putAll(Map<? extends String, ? extends Object> t)
    {
        this.data.putAll(t);
    }

    public void clear()
    {
        data.clear();
    }

    public Collection<Object> values()
    {
        return data.values();
    }

    public Set<Entry<String, Object>> entrySet()
    {
        return data.entrySet();
    }
}
