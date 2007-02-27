package com.zutubi.prototype.type.record;

import com.zutubi.prototype.type.TypeRegistry;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

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
        // Should this be templated???
        // Currently only holds the type.
        // TODO
        return null;
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
        Object value = moi.get(key);

        if(value instanceof Record)
        {
            Object inherited = parent == null ? null : parent.get(key);
            return new TemplateRecord(owner, (TemplateRecord)inherited, (Record)value);
        }

        return value;
    }

    public Object put(String key, Object value)
    {
        return moi.put(key, value);
    }

    public Object remove(Object key)
    {
        return moi.remove(key);
    }

    public void putAll(Map<? extends String, ? extends Object> t)
    {
        moi.putAll(t);
    }

    public void clear()
    {
        // I don't suppose we'll actually use this ...
        moi.clear();
        if(parent != null)
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

    public Set<Entry<String, Object>> entrySet()
    {
        return getMergedMap().entrySet();
    }

    private Map<String, Object> getMergedMap()
    {
        // Actually, do we really need this annoying Map interface??
        // TODO
        return null;
    }

    public Record clone() throws CloneNotSupportedException
    {
        // TODO
        return null;
    }

    /**
     * Would be nice to have a flattened view of the template information, since this is the view
     * that will be commonly used when displaying forms. 
     *
     * @return a flattened record view of this template record.
     */
    public MutableRecord flatten()
    {
        // do not forget that this needs to set the template records symbolic details.
        return new MutableRecord();
    }
}
