package com.zutubi.prototype.type;

import com.zutubi.config.annotations.Ordered;
import com.zutubi.prototype.type.record.*;

import java.util.Arrays;
import java.util.Set;

/**
 *
 *
 */
public abstract class CollectionType extends AbstractType implements ComplexType
{
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

    public Iterable<String> getOrder(Record record)
    {
        Set<String> keys = record.keySet();

        if (getAnnotation(Ordered.class) != null)
        {
            String order = record.getMeta("order");
            if (order != null)
            {
                return convertOrder(order);
            }
        }

        // By default, just whatever order.
        return keys;
    }

    private Iterable<String> convertOrder(String order)
    {
        // FIXME comma not safe in keys, could be bad.
        return Arrays.asList(order.split(","));
    }

    public MutableRecord createNewRecord()
    {
        return new MutableRecordImpl();
    }

    public boolean isTemplated()
    {
        return false;
    }
}
