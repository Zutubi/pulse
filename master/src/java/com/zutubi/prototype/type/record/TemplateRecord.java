package com.zutubi.prototype.type.record;

import com.zutubi.prototype.type.CollectionType;
import com.zutubi.prototype.type.ComplexType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 */
public class TemplateRecord extends AbstractRecord
{
    private String owner;
    private TemplateRecord parent;
    private ComplexType type;
    private Record moi;
    private Collection<String> declaredOrder = null;

    public TemplateRecord(String owner, TemplateRecord parent, ComplexType type, Record moi)
    {
        this.owner = owner;
        this.parent = parent;
        this.type = type;
        this.moi = moi;

        if(type instanceof CollectionType)
        {
            declaredOrder = CollectionType.getDeclaredOrder(moi);
        }
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

    public boolean containsKey(String key)
    {
        return moi.containsKey(key) || parent != null && parent.containsKey(key);
    }

    public boolean containsValue(String value)
    {
        return values().contains(value);
    }

    public Object get(String key)
    {
        // This is where magic happens.
        Object value = moi.get(key);
        if (value == null)
        {
            // We have nothing to add, delegate to parent
            return getInherited(key);
        }
        else if (value instanceof Record)
        {
            return new TemplateRecord(owner, (TemplateRecord) getInherited(key), (ComplexType) type.getActualPropertyType(key, value), (MutableRecord) value);
        }
        else
        {
            // Primitive, we override the parent
            return value;
        }
    }

    private Object getInherited(String key)
    {
        // If we have no parent or we have a declared order that omits this
        // key, we cannot inherit a value.
        if(parent == null || declaredOrder != null && !declaredOrder.contains(key))
        {
            return null;
        }

        return parent.get(key);
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
        Map<String, Object> mergedMap = new HashMap<String, Object>();

        for (String key : moi.keySet())
        {
            mergedMap.put(key, moi.get(key));
        }

        if (parent != this && parent != null) // ensure that we do not recurse infinitely -> IDEA complaint.
        {
            Map<String, Object> parentMerged = parent.getMergedMap();
            if(declaredOrder == null)
            {
                mergedMap.putAll(parentMerged);
            }
            else
            {
                for(Map.Entry<String, Object> entry: parentMerged.entrySet())
                {
                    if(declaredOrder.contains(entry.getKey()))
                    {
                        mergedMap.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }

        return mergedMap;
    }

    public MutableRecord flatten()
    {
        // FIXME NYI
        return new MutableRecordImpl();
    }

    public TemplateRecord getParent()
    {
        return parent;
    }

    public String getOwner()
    {
        return owner;
    }

    public ComplexType getType()
    {
        return type;
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

    public MutableRecord copy(boolean deep)
    {
        throw new UnsupportedOperationException("Record is not mutable.");
    }

    public Set<String> simpleKeySet()
    {
        // FIXME: should only return keys for simple properties
        return getMergedMap().keySet();
    }

    public Record getMoi()
    {
        return moi;
    }
}
