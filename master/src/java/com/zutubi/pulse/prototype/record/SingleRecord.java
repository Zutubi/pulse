package com.zutubi.pulse.prototype.record;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A Record is an object graph converted to a simpified data structure for
 * the purpose of persistence and templating.
 */
public class SingleRecord implements Record
{
    private String symbolicName;

    Map<String, Object> data = new TreeMap<String, Object>();

    public SingleRecord(String symbolicName)
    {
        this.symbolicName = symbolicName;
    }

    public String getSymbolicName()
    {
        return symbolicName;
    }

    public void setSymbolicName(String symbolicName)
    {
        this.symbolicName = symbolicName;
    }

    public Object get(String name)
    {
        return data.get(name);
    }

    public int size()
    {
        return data.size();
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

    public Object put(String key, Object value)
    {
        return data.put(key, value);
    }

    public Object remove(Object key)
    {
        return data.remove(key);
    }

    public void putAll(Map<? extends String, ? extends Object> t)
    {
        data.putAll(t);
    }

    public void clear()
    {
        data.clear();
    }

    public Set<String> keySet()
    {
        return data.keySet();
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
