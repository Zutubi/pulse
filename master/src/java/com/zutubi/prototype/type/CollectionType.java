package com.zutubi.prototype.type;

import com.zutubi.prototype.annotation.Ordered;
import com.zutubi.prototype.type.record.*;

import java.util.Arrays;
import java.util.Set;

/**
 *
 *
 */
public abstract class CollectionType extends AbstractType implements ComplexType
{
    private static final String LATEST_KEY_KEY = "latestKey";

    private Type collectionType;

    public CollectionType(Class type)
    {
        super(type);
    }

    public CollectionType(Class type, String symbolicName)
    {
        super(type, symbolicName);
    }

    public Type getCollectionType()
    {
        return collectionType;
    }

    public void setCollectionType(Type collectionType)
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
        // TODO: comma not safe in keys, could be bad.
        return Arrays.asList(order.split(","));
    }

    public String insert(String path, Record newRecord, RecordManager recordManager)
    {
        Record collectionRecord = recordManager.load(path);
        if (collectionRecord == null)
        {
            throw new IllegalArgumentException("Attempt to store into a non-existant list at path '" + path + "'");
        }

        String newKey = getNextKey(path, collectionRecord, recordManager);
        String newPath = PathUtils.getPath(path, newKey);
        recordManager.insert(newPath, newRecord);
        return newPath;
    }

    public Record createNewRecord()
    {
        return new MutableRecordImpl();
    }

    public boolean isTemplated()
    {
        return false;
    }

    protected String getNextKey(String path, Record record, RecordManager recordManager)
    {
        String latestKey = record.getMeta(LATEST_KEY_KEY);
        if (latestKey == null)
        {
            latestKey = "1";
        }
        else
        {
            latestKey = Integer.toString(Integer.parseInt(latestKey) + 1);
        }

        MutableRecord mutableRecord = record.createMutable();
        mutableRecord.putMeta(LATEST_KEY_KEY, latestKey);
        recordManager.store(path, mutableRecord);
        return latestKey;
    }
}
