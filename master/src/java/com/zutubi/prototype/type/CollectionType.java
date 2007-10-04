package com.zutubi.prototype.type;

import com.zutubi.config.annotations.Ordered;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.MutableRecordImpl;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.util.StringUtils;

import java.util.*;

/**
 * Base for types that represent collections like maps and lists.
 */
public abstract class CollectionType extends AbstractType implements ComplexType
{
    public static final String HIDDEN_KEY = "hidden";
    public static final String ORDER_KEY  = "order";

    private Type collectionType;

    public CollectionType(Class type)
    {
        super(type);
    }

    public CollectionType(Class type, String symbolicName)
    {
        super(type, symbolicName);
    }

    public Type getTargetType()
    {
        return collectionType;
    }

    public Type getCollectionType()
    {
        return collectionType;
    }

    public void setCollectionType(Type collectionType) throws TypeException
    {
        this.collectionType = collectionType;
    }

    public static Collection<String> getDeclaredOrder(Record record)
    {
        String order = record.getMeta(ORDER_KEY);
        if (order == null)
        {
            return null;
        }

        if(order.length() == 0)
        {
            return new ArrayList<String>(0);
        }

        return split(order);
    }

    public Collection<String> getOrder(Record record)
    {
        Collection<String> order = getDeclaredOrder(record);
        if(order == null)
        {
            List<String> defaultOrder = new ArrayList<String>(record.keySet());
            Collections.sort(defaultOrder, getKeyComparator());
            order = defaultOrder;
        }

        return order;
    }

    protected void setOrder(MutableRecord record, Collection<String> order)
    {
        record.putMeta(ORDER_KEY, join(order));
    }

    public void hideItem(MutableRecord record, String key)
    {
        Set<String> hiddenKeys = getHiddenKeys(record);
        hiddenKeys.add(key);
        record.putMeta(HIDDEN_KEY, join(hiddenKeys));
    }

    public void unhideItem(MutableRecord record, String key)
    {
        
    }

    private Set<String> getHiddenKeys(MutableRecord record)
    {
        String hidden = record.getMeta(HIDDEN_KEY);
        if(hidden == null)
        {
            return new HashSet<String>();
        }
        else
        {
            return new HashSet<String>(split(hidden));
        }
    }

    private static String join(Collection<String> c)
    {
        StringBuilder result = new StringBuilder(c.size() * 32);
        for(String s: c)
        {
            if(result.length() > 0)
            {
                result.append(',');
            }

            result.append(StringUtils.uriComponentDecode(s));
        }

        return result.toString();
    }

    private static Collection<String> split(String s)
    {
        String[] pieces = s.split(",");
        List<String> result = new ArrayList<String>(pieces.length);
        for(String item: pieces)
        {
            result.add(StringUtils.uriComponentEncode(item));
        }
        return result;
    }

    protected abstract Comparator<String> getKeyComparator();

    @SuppressWarnings({"unchecked"})
    public MutableRecord createNewRecord(boolean applyDefaults)
    {
        return new MutableRecordImpl();
    }

    public boolean isOrdered()
    {
        return getAnnotation(Ordered.class) != null;
    }

    public boolean isTemplated()
    {
        return false;
    }

    public Type getDeclaredPropertyType(String propertyName)
    {
        return collectionType;
    }

    public Type getActualPropertyType(String propertyName, Object propertyValue)
    {
        return collectionType.getActualType(propertyValue);
    }
}
