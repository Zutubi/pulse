package com.zutubi.prototype.type.record;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.util.*;

/**
 * Simple record that holds key:value data, along with meta data.
 */
public class MutableRecordImpl extends AbstractMutableRecord
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

    public boolean containsKey(String key)
    {
        return data.containsKey(key);
    }

    public Object get(String key)
    {
        return data.get(key);
    }

    public Object remove(String key)
    {
        return data.remove(key);
    }

    public void clear()
    {
        meta.clear();
        data.clear();
    }

    public MutableRecord copy(boolean deep)
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
            if (deep && value instanceof Record)
            {
                value = ((Record) value).copy(true);
            }
            clone.put(key, value);
        }
        return clone;
    }

    public void update(Record record)
    {
        // take the new primitive data from the record.
        for (String key : record.keySet())
        {
            Object value = record.get(key);
            if(!(value instanceof Record))
            {
                data.put(key, value);
            }
        }
    }

    public Set<String> simpleKeySet()
    {
        return CollectionUtils.filter(keySet(), new Predicate<String>()
        {
            public boolean satisfied(String s)
            {
                return !(get(s) instanceof Record);
            }
        }, new HashSet<String>(data.size()));
    }

    public Collection<Object> values()
    {
        return data.values();
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        MutableRecordImpl that = (MutableRecordImpl) o;

        if (data != null ? !data.equals(that.data) : that.data != null)
        {
            return false;
        }

        return !(meta != null ? !meta.equals(that.meta) : that.meta != null);
    }

    public int hashCode()
    {
        int result;
        result = (meta != null ? meta.hashCode() : 0);
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }
}
