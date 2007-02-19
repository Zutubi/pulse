package com.zutubi.prototype.type;

import com.zutubi.prototype.type.record.Record;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 *
 */
public class MapType extends CollectionType
{
    public MapType()
    {
        this(HashMap.class);
    }

    public MapType(Class type)
    {
        super(type);
    }

    public MapType(Class type, String symbolicName)
    {
        super(type, symbolicName);
    }

    public Map instantiate(Object data) throws TypeException
    {
        if (data == null)
        {
            return null;
        }

        if (!Map.class.isAssignableFrom(data.getClass()))
        {
            throw new TypeConversionException("Expected a map type, instead received " + data.getClass());
        }

        Record record = (Record)data;

        Type defaultType = getCollectionType();
        if (defaultType == null && record.getMeta("type") != null)
        {
            defaultType = typeRegistry.getType(record.getMeta("type"));
        }

        Map<String, Object> instance = new HashMap<String, Object>();
        for (String key : record.keySet())
        {
            Object child = record.get(key);
            Type type = defaultType;
            if (child instanceof Record)
            {
                Record childRecord = (Record) child;
                type = typeRegistry.getType(childRecord.getSymbolicName());
            }

            Object value = type.instantiate(child);
            instance.put(key, value);
        }

        return instance;
    }

    public Map<String, Object> instantiate() throws TypeConversionException
    {
        return new HashMap<String, Object>();
    }

    public Record unstantiate(Object data) throws TypeException
    {
        if (data == null)
        {
            return null;
        }

        Map<String, Object> map = (Map<String, Object>) data;

        Record record = new Record();
        record.setSymbolicName("mapType");

        // write type data.
        if (map.size() > 0)
        {
            Object first = map.values().iterator().next();
            Type type = typeRegistry.getType(first.getClass());
            if (type instanceof PrimitiveType)
            {
                record.putMeta("type", type.getSymbolicName());
            }
        }

        for (String key: map.keySet())
        {
            Object obj = map.get(key);
            Type objectType = typeRegistry.getType(obj.getClass());
            record.put(key, objectType.unstantiate(obj));
        }
        return record;
    }
}
