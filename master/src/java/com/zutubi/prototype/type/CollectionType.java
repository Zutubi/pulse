package com.zutubi.prototype.type;

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
    public static final String ORDER_KEY  = "order";

    private static final char SEPARATOR = ',';

    private Type collectionType;
    private boolean ordered = false;

    public CollectionType(Class type, Type collectionType, TypeRegistry typeRegistry) throws TypeException
    {
        super(type);
        verifyCollectionType(collectionType);
        this.collectionType = collectionType;
        this.typeRegistry = typeRegistry;
    }

    protected void verifyCollectionType(Type collectionType) throws TypeException
    {
        if(collectionType instanceof CollectionType)
        {
            throw new TypeException("Collection cannot itself hold a collection type");
        }
    }

    public Type getTargetType()
    {
        return collectionType;
    }

    public Type getCollectionType()
    {
        return collectionType;
    }

    public static List<String> getDeclaredOrder(Record record)
    {
        String order = record.getMeta(ORDER_KEY);
        if(order == null || order.length() == 0)
        {
            return new LinkedList<String>();
        }

        return new LinkedList(StringUtils.splitAndDecode(SEPARATOR, order));
    }

    public List<String> getOrder(Record record)
    {
        List<String> order = getDeclaredOrder(record);

        // Remove non-existant keys
        Set<String> keySet = record.keySet();
        Iterator<String> it = order.iterator();
        while (it.hasNext())
        {
            String key = it.next();

            // Filter out keys that do not exist (can validly happen with
            // hidden items and inherited orders).
            if(!keySet.contains(key))
            {
                it.remove();
            }
        }

        // Add remaining keys (i.e. those not represented in the order)
        List<String> remaining = new LinkedList<String>(keySet);
        remaining.removeAll(order);
        Collections.sort(remaining, getKeyComparator(record));
        order.addAll(remaining);

        return order;
    }

    public static void setOrder(MutableRecord record, Collection<String> order)
    {
        record.putMeta(ORDER_KEY, StringUtils.encodeAndJoin(SEPARATOR, order));
    }

    public abstract Comparator<String> getKeyComparator(Record record);

    @SuppressWarnings({"unchecked"})
    public MutableRecord createNewRecord(boolean applyDefaults)
    {
        return new MutableRecordImpl();
    }

    public boolean isOrdered()
    {
        return ordered;
    }

    public void setOrdered(boolean ordered)
    {
        this.ordered = ordered;
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
