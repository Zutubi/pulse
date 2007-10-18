package com.zutubi.prototype.type.record;

import com.zutubi.config.annotations.NoInherit;
import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.prototype.type.CollectionType;
import com.zutubi.prototype.type.ComplexType;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 */
public class TemplateRecord extends AbstractRecord
{
    public static final String HIDDEN_KEY = "hidden";

    private static final char SEPARATOR = ',';
    private static final String[] NO_INHERIT_META_KEYS = { HANDLE_KEY, HIDDEN_KEY, PERMANENT_KEY, SYMBOLIC_NAME_KEY,
                                                           ConfigurationTemplateManager.PARENT_KEY,
                                                           ConfigurationTemplateManager.TEMPLATE_KEY };
    /**
     * Id of the owning object.  For example, the name of the project his
     * record lives in.  This may be empty for new template records.
     */
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
        if (value == null && canInheritMeta(key))
        {
            value = parent.getMeta(key);
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
        if(getHiddenKeys(moi).contains(key))
        {
            return null;
        }

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

        return !markedNoInherit(key);
    }

    private boolean markedNoInherit(String key)
    {
        // Composite properties explicitly marked NoInherit cannot be
        // inherited.
        if (type instanceof CompositeType)
        {
            CompositeType ctype = (CompositeType) type;
            TypeProperty property = ctype.getProperty(key);
            if (property != null && property.getAnnotation(NoInherit.class) != null)
            {
                return true;
            }
        }
        return false;
    }

    private boolean canInheritMeta(String key)
    {
        // No parent, nothing to inherit.
        if (parent == null)
        {
            return false;
        }

        return !CollectionUtils.contains(NO_INHERIT_META_KEYS, key);
    }

    public Set<String> keySet()
    {
        Set<String> result;
        if (declaredOrder != null)
        {
            result = new HashSet<String>(declaredOrder);
        }
        else
        {
            result = parent == null ? new HashSet<String>() : new HashSet<String>(parent.keySet());
            result.addAll(moi.keySet());
        }

        result.removeAll(getHiddenKeys(moi));
        return result;
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
        if (isSignificant(key, moi.get(key)))
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

    private boolean isSignificant(String key, Object value)
    {
        // If we can't inherit a composite property, we own it even if it is null.
        if (!canInherit(key) && type instanceof CompositeType)
        {
            return true;
        }

        if (value == null)
        {
            return false;
        }

        if (value instanceof Record)
        {
            TemplateRecord templateRecord = (TemplateRecord) get(key);
            return !templateRecord.isSkeleton();
        }

        return true;
    }

    public boolean isSkeleton()
    {
        // If we do not override anything, then we are a skeleton.
        for(String metaKey: moi.metaKeySet())
        {
            if(!CollectionUtils.contains(NO_INHERIT_META_KEYS, metaKey))
            {
                return false;
            }
        }

        if(moi.simpleKeySet().size() > 0)
        {
            return false;
        }

        if(getHiddenKeys(moi).size() > 0)
        {
            return false;
        }

        for(String key: nestedKeySet())
        {
            if(!((TemplateRecord)get(key)).isSkeleton())
            {
                return false;
            }
        }

        return true;
    }

    public MutableRecord copy(boolean deep)
    {
        throw new UnsupportedOperationException("Record is not mutable.");
    }

    public Set<String> getHiddenKeys()
    {
        return getHiddenKeys(moi);
    }

    public static void hideItem(MutableRecord record, String key)
    {
        Set<String> hiddenKeys = getHiddenKeys(record);
        hiddenKeys.add(key);
        record.putMeta(HIDDEN_KEY, StringUtils.encodeAndJoin(SEPARATOR, hiddenKeys));
    }

    public static boolean restoreItem(MutableRecord record, String key)
    {
        Set<String> hiddenKeys = getHiddenKeys(record);
        boolean result = hiddenKeys.remove(key);
        if(hiddenKeys.size() == 0)
        {
            record.removeMeta(HIDDEN_KEY);
        }
        else
        {
            record.putMeta(HIDDEN_KEY, StringUtils.encodeAndJoin(SEPARATOR, hiddenKeys));
        }

        return result;
    }

    public static Set<String> getHiddenKeys(Record record)
    {
        String hidden = record.getMeta(HIDDEN_KEY);
        if(hidden == null)
        {
            return new HashSet<String>();
        }
        else
        {
            return new HashSet<String>(StringUtils.splitAndDecode(SEPARATOR, hidden));
        }
    }
}
