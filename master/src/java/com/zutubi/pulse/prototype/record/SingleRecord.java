package com.zutubi.pulse.prototype.record;

import com.zutubi.pulse.prototype.Scope;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * <class comment/>
 */
public class SingleRecord implements Record
{
    private Scope scope;
    private String id;
    private String symbolicName;

    Map<String, String> data = new TreeMap<String, String>();

    public Map<String, String> getData()
    {
        return this;
    }

    public String getSymbolicName()
    {
        return symbolicName;
    }

    public String get(String name)
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

    public String get(Object key)
    {
        return data.get(key);
    }

    public String put(String key, String value)
    {
        return data.put(key, value);
    }

    public String remove(Object key)
    {
        return data.remove(key);
    }

    public void putAll(Map<? extends String, ? extends String> t)
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

    public Collection<String> values()
    {
        return data.values();
    }

    public Set<Entry<String, String>> entrySet()
    {
        return data.entrySet();
    }
}
