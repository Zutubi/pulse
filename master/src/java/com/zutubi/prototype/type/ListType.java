package com.zutubi.prototype.type;

import com.zutubi.prototype.type.record.Record;

import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class ListType extends CollectionType
{
    public ListType()
    {
        this(LinkedList.class);
    }

    public ListType(Class type)
    {
        super(type);
    }

    public ListType(Class type, String symbolicName)
    {
        super(type, symbolicName);
    }

    public List<Object> instantiate(Object data) throws TypeException
    {
        if (data == null)
        {
            return null;
        }

        if (data instanceof Record)
        {
            Record record = (Record) data;

            Iterable<String> keys = getOrder(record);
            Type defaultType = getCollectionType();
            if (defaultType == null && record.getMeta("type") != null)
            {
                defaultType = typeRegistry.getType(record.getMeta("type"));
            }

            List<Object> instance = new LinkedList<Object>();
            for (String key : keys)
            {
                Object child = record.get(key);
                Type type = defaultType;
                if (child instanceof Record)
                {
                    Record childRecord = (Record) child;
                    type = typeRegistry.getType(childRecord.getSymbolicName());
                }
                Object value = type.instantiate(child);
                instance.add(value);
            }
            
            return instance;
        }
        else if(data instanceof String[])
        {
            List<Object> instance = new LinkedList<Object>();
            for(String s: (String[])data)
            {
                instance.add(s);
            }
            return instance;
        }
        else
        {
            throw new TypeConversionException("Expected a record or string array, instead received " + data.getClass());
        }
    }
}
