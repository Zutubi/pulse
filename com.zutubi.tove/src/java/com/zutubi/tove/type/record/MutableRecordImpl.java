package com.zutubi.tove.type.record;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Simple record that holds key:value data, along with meta data.
 */
public class MutableRecordImpl extends AbstractMutableRecord 
{
    private Map<String, String> meta = null;

    private Map<String, Object> data = null;

    public void setSymbolicName(String name)
    {
        if (name == null)
        {
            getMeta().remove(SYMBOLIC_NAME_KEY);
        }
        else
        {
            getMeta().put(SYMBOLIC_NAME_KEY, name);
        }
    }

    public String getSymbolicName()
    {
        return getMeta().get(SYMBOLIC_NAME_KEY);
    }

    public void putMeta(String key, String value)
    {
        if (value == null)
        {
            throw new NullPointerException();
        }
        getMeta().put(key, value);
    }

    public String removeMeta(String key)
    {
        return getMeta().remove(key);
    }

    public String getMeta(String key)
    {
        return getMeta().get(key);
    }

    public Object put(String key, Object value)
    {
        if (value == null)
        {
            throw new NullPointerException();
        }
        return getData().put(key, value);
    }

    public int size()
    {
        return getData().size();
    }

    public Set<String> keySet()
    {
        return getData().keySet();
    }

    public Set<String> metaKeySet()
    {
        return getMeta().keySet();
    }

    public boolean containsMetaKey(String key)
    {
        return getMeta().containsKey(key);
    }

    public boolean containsKey(String key)
    {
        return getData().containsKey(key);
    }

    public Object get(String key)
    {
        return getData().get(key);
    }

    public Object remove(String key)
    {
        return getData().remove(key);
    }

    public void clear()
    {
        getMeta().clear();
        getData().clear();
    }

    public MutableRecord copy(boolean deep, boolean preserveHandles)
    {
        MutableRecordImpl clone = new MutableRecordImpl();

        for (Map.Entry<String, String> entry : getMeta().entrySet())
        {
            String key = entry.getKey();
            String value = entry.getValue();
            if (preserveHandles || !key.equals(HANDLE_KEY))
            {
                clone.putMeta(key, value);
            }
        }

        for (Map.Entry<String, Object> entry : getData().entrySet())
        {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (deep && value instanceof Record)
            {
                value = ((Record) value).copy(deep, preserveHandles);
            }
            clone.put(key, value);
        }
        return clone;
    }

    public void update(Record record)
    {
        // take the new primitive data from the record.
        for (String key : record.metaKeySet())
        {
            getMeta().put(key, record.getMeta(key));
        }

        for (String key : record.simpleKeySet())
        {
            getData().put(key, record.get(key));
        }
    }

    public Collection<Object> values()
    {
        return getData().values();
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

        return super.equals(o);
    }

    private Map<String, String> getMeta()
    {
        if (meta == null)
        {
            meta = new HashMap<String, String>();
        }
        return meta;
    }

    private Map<String, Object> getData()
    {
        if (data == null)
        {
            data = new HashMap<String, Object>();
        }
        return data;
    }

    protected Object clone() throws CloneNotSupportedException
    {
        super.clone();
        return copy(true, true);
    }
}
