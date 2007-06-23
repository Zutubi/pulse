package com.zutubi.prototype.type.record;

import com.zutubi.config.annotations.NoInherit;
import com.zutubi.prototype.type.CollectionType;
import com.zutubi.prototype.type.ComplexType;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.TypeProperty;

import java.util.Collection;
import java.util.HashSet;
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

        if (type instanceof CollectionType)
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
        if (canInherit(key))
        {
            return parent.get(key);
        }
        else
        {
            return null;
        }
    }

    private boolean canInherit(String key)
    {
        // No parent, nothing to inherit.
        if (parent == null)
        {
            return false;
        }

        // If we have a declared order that omits this key, we cannot inherit
        // a value.
        if (declaredOrder != null && !declaredOrder.contains(key))
        {
            return false;
        }

        // Composite properties explicitly marked NoInherit cannot be
        // inherited.
        if (type instanceof CompositeType)
        {
            CompositeType ctype = (CompositeType) type;
            TypeProperty property = ctype.getProperty(key);
            if (property != null && property.getAnnotation(NoInherit.class) != null)
            {
                return false;
            }
        }

        return true;
    }

    public Set<String> keySet()
    {
        if (declaredOrder != null)
        {
            return new HashSet<String>(declaredOrder);
        }
        else
        {
            Set<String> set = parent == null ? new HashSet<String>() : new HashSet<String>(parent.keySet());
            set.addAll(moi.keySet());
            return set;
        }
    }

    public Set<String> metaKeySet()
    {
        Set<String> set = parent == null ? new HashSet<String>() : new HashSet<String>(parent.metaKeySet());
        set.addAll(moi.metaKeySet());
        return set;
    }

    public Collection<Object> values()
    {
        return flatten().values();
    }

    public MutableRecord flatten()
    {
        MutableRecord record = new MutableRecordImpl();
        for (String metaKey : metaKeySet())
        {
            if (!metaKey.equals(HANDLE_KEY))
            {
                record.putMeta(metaKey, getMeta(metaKey));
            }
        }

        for (String key : keySet())
        {
            Object value = get(key);
            if (value instanceof TemplateRecord)
            {
                value = ((TemplateRecord) value).flatten();
            }

            record.put(key, value);
        }

        return record;
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

    public Record getMoi()
    {
        return moi;
    }

    public String getOwner(String key)
    {
        if (isSignificant(type, key, moi.get(key)))
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

    private boolean isSignificant(ComplexType type, String key, Object value)
    {
        // If we can't inherit a value, we own it even if it is null.
        if (!canInherit(key))
        {
            return true;
        }

        if (value == null)
        {
            return false;
        }

        if (value instanceof Record)
        {
            Record record = (Record) value;
            if (record.isCollection())
            {
                // Only ordered collections have an owner, and that is
                // whoever's order applies.  If there is no order, the
                // record is owned by the root of the template tree.
                return CollectionType.getDeclaredOrder(record) != null || parent == null;
            }
            else
            {
                CompositeType actualType = (CompositeType) type.getActualPropertyType(key, value);
                for (String property : actualType.getPropertyNames())
                {
                    if (isSignificant(actualType, property, record.get(property)))
                    {
                        return true;
                    }
                }

                return false;
            }
        }

        return true;
    }

    public MutableRecord copy(boolean deep)
    {
        throw new UnsupportedOperationException("Record is not mutable.");
    }
}
