package com.zutubi.prototype.type.record;

import com.zutubi.prototype.type.TypeRegistry;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

/**
 */
public class TemplateRecord implements Record
{
    private TypeRegistry registry;
    private TemplateRecord parent;
    private Record moi;
    private String owner;

    public TemplateRecord(String owner, TemplateRecord parent, Record moi)
    {
        this.owner = owner;
        this.parent = parent;
        this.moi = moi;
    }

    public void setSymbolicName(String name)
    {
        moi.setSymbolicName(name);
    }

    public String getSymbolicName()
    {
        return moi.getSymbolicName();
    }

    public void putMeta(String key, String value)
    {
        moi.putMeta(key, value);
    }

    public String getMeta(String key)
    {
        String value = moi.getMeta(key);
        if (value == null)
        {
            value = parent == null ? null : parent.getMeta(key);
        }

        return value;
    }

    public int size()
    {
        return keySet().size();
    }

    public boolean isEmpty()
    {
        return keySet().isEmpty();
    }

    public boolean containsKey(Object key)
    {
        return moi.containsKey(key) || parent != null && parent.containsKey(key);
    }

    public boolean containsValue(Object value)
    {
        return values().contains(value);
    }

    public Object get(Object key)
    {
        // This is where magic happens.
        Object value = moi.get(key);
        if (value == null)
        {
            // We have nothing to add, delegate to parent
            return getInherited((String) key);
        }
        else if (value instanceof Record)
        {
            // Wrap in another template on the way out
            return new TemplateRecord(owner, (TemplateRecord) getInherited((String) key), (Record) value);
        }
        else
        {
            // Primitive, we override the parent
            return value;
        }
    }

    private Object getInherited(String key)
    {
        return parent == null ? null : parent.get(key);
    }

    public Object put(String key, Object value)
    {
        return moi.put(key, value);
    }

    public Object remove(Object key)
    {
        return moi.remove(key);
    }

    public void putAll(Record t)
    {
        moi.putAll(t);
    }

    public void clear()
    {
        // I don't suppose we'll actually use this ...
        moi.clear();
        if (parent != null)
        {
            parent.clear();
        }
    }

    public Set<String> keySet()
    {
        return getMergedMap().keySet();
    }

    public Collection<Object> values()
    {
        return getMergedMap().values();
    }

    public Set<Map.Entry<String, Object>> entrySet()
    {
        return getMergedMap().entrySet();
    }

    private Map<String, Object> getMergedMap()
    {
        // Actually, do we really need this annoying Map interface??
        // TODO
        return new HashMap<String, Object>();
    }

    public Record clone() throws CloneNotSupportedException
    {
        // TODO
        return null;
    }

    public MutableRecordImpl flatten()
    {
        return new MutableRecordImpl();
    }

    public String getOwner(String key)
    {
        if (moi.containsKey(key))
        {
            return owner;
        }
        else if (parent != null)
        {
            return parent.getOwner(key);
        }
        else
        {
            return null;
        }
    }
}
