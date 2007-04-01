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

    public String getSymbolicName()
    {
        return moi.getSymbolicName();
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
            return new TemplateRecord(owner, (TemplateRecord) getInherited((String) key), (MutableRecord) value);
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

    public Set<String> keySet()
    {
        return getMergedMap().keySet();
    }

    public Set<String> metaKeySet()
    {
        return moi.metaKeySet();
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

    public Record flatten()
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

    public MutableRecord createMutable()
    {
        throw new UnsupportedOperationException("Record is not mutable.");
    }
}
