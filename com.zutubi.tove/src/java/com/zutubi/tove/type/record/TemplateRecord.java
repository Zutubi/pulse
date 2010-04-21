package com.zutubi.tove.type.record;

import com.zutubi.tove.annotations.NoInherit;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.ComplexType;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.WebUtils;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 */
public class TemplateRecord extends AbstractRecord
{
    public static final String HIDDEN_KEY    = "hidden";
    public static final String PARENT_KEY    = "parentHandle";
    public static final String TEMPLATE_KEY  = "template";

    public static final String[] NO_INHERIT_META_KEYS = { HANDLE_KEY, HIDDEN_KEY, PERMANENT_KEY, SYMBOLIC_NAME_KEY,
                                                           PARENT_KEY,
                                                           TEMPLATE_KEY };

    private static final char SEPARATOR = ',';

    /**
     * Id of the owning object.  For example, the name of the project his
     * record lives in.  This may be empty for new template records.
     */
    private String owner;
    private TemplateRecord parent;
    private ComplexType type;
    private Record moi;

    public TemplateRecord(String owner, TemplateRecord parent, ComplexType type, Record moi)
    {
        this.owner = owner;
        this.parent = parent;
        this.type = type;
        this.moi = moi;
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

    public boolean containsMetaKey(String key)
    {
        return metaKeySet().contains(key);
    }

    public boolean containsKey(String key)
    {
        return keySet().contains(key);
    }

    public boolean containsValue(String value)
    {
        return values().contains(value);
    }

    public Object get(String key)
    {
        // This is where magic happens.
        if(getHiddenKeys().contains(key))
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
        return parent != null && !isPropertyAnnotated(key, NoInherit.class);
    }

    private boolean isPropertyAnnotated(String key, Class<? extends Annotation> annotationClass)
    {
        // Composite properties explicitly marked NoInherit cannot be
        // inherited.
        if (type instanceof CompositeType)
        {
            CompositeType ctype = (CompositeType) type;
            TypeProperty property = ctype.getProperty(key);
            if (property != null && property.getAnnotation(annotationClass) != null)
            {
                return true;
            }
        }
        return false;
    }

    private boolean isInternalProperty(String key)
    {
        if (type instanceof CompositeType)
        {
            CompositeType ctype = (CompositeType) type;
            return ctype.hasInternalProperty(key);
        }
        return false;
    }

    private boolean canInheritMeta(String key)
    {
        // No parent, nothing to inherit.
        return parent != null && !CollectionUtils.contains(NO_INHERIT_META_KEYS, key);
    }

    public Set<String> keySet()
    {
        Set<String> result = new HashSet<String>(moi.keySet());
        if(parent != null)
        {
            result.addAll(parent.keySet());
        }
        
        result.removeAll(getHiddenKeys());
        return result;
    }

    public Set<String> metaKeySet()
    {
        Set<String> set = parent == null ? new HashSet<String>() : new HashSet<String>(parent.metaKeySet());
        for(String key: NO_INHERIT_META_KEYS)
        {
            set.remove(key);
        }
        set.addAll(moi.metaKeySet());
        return set;
    }

    public Collection<Object> values()
    {
        return flatten(false).values();
    }

    /**
     * Flattens this templated record into a regular record with the same
     * contents.  Inherited values are represented directly in the returned
     * record.
     * 
     * @param preserveHandles if true, the returned records will maintain the
     *                        handles in this record (from the lowest level),
     *                        if false the returned records will have no
     *                        handles
     * @return a flattened version of this record
     */
    public MutableRecord flatten(boolean preserveHandles)
    {
        MutableRecord record = new MutableRecordImpl();
        for (String metaKey : metaKeySet())
        {
            if (preserveHandles || !metaKey.equals(Configuration.HANDLE_KEY))
            {
                record.putMeta(metaKey, getMeta(metaKey));
            }
        }

        for (String key : keySet())
        {
            Object value = get(key);
            if (value != null)
            {
                if (value instanceof TemplateRecord)
                {
                    value = ((TemplateRecord) value).flatten(preserveHandles);
                }

                record.put(key, value);
            }
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

    /**
     * Returns the name of the templated instance that owns the given property.
     * The owner is the instance where the property is first defined with its
     * local value.  So, if getOwner(key).equals(getOwner()), then we override
     * the property locally, otherwise we inherit it from an ancestor.
     * 
     * @param key name of the property to get the owner for
     * @return name of the instance that owns the property (i.e. defines its
     *         local value).
     */
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

    public String getMetaOwner(String key)
    {
        if(moi.metaKeySet().contains(key))
        {
            return owner;
        }
        else if(parent != null)
        {
            return parent.getMetaOwner(key);
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

        for(String simpleKey: moi.simpleKeySet())
        {
            if(!isInternalProperty(simpleKey))
            {
                return false;
            }
        }

        if(getHiddenKeys().size() > 0)
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

    public MutableRecord copy(boolean deep, boolean preserveHandles)
    {
        return flatten(false);
    }

    public Set<String> getHiddenKeys()
    {
        return getHiddenKeys(moi);
    }

    public static void hideItem(MutableRecord record, String key)
    {
        Set<String> hiddenKeys = getHiddenKeys(record);
        hiddenKeys.add(key);
        record.putMeta(HIDDEN_KEY, WebUtils.encodeAndJoin(SEPARATOR, hiddenKeys));
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
            record.putMeta(HIDDEN_KEY, WebUtils.encodeAndJoin(SEPARATOR, hiddenKeys));
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
            return new HashSet<String>(WebUtils.splitAndDecode(SEPARATOR, hidden));
        }
    }
}
