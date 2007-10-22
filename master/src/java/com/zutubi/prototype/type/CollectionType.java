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
    public static final String ORDER_KEY  = "order";

    private static final char SEPARATOR = ',';

    private Type collectionType;

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

        return StringUtils.splitAndDecode(SEPARATOR, order);
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
        record.putMeta(ORDER_KEY, StringUtils.encodeAndJoin(SEPARATOR, order));
    }

    public abstract Comparator<String> getKeyComparator();

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
