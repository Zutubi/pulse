package com.zutubi.prototype.type;

import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.RecordManager;

import java.util.Collections;
import java.util.Comparator;
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

        if (!(data instanceof Record))
        {
            throw new TypeConversionException("Expected a map type, instead received " + data.getClass());
        }

        Record record = (Record)data;
        
        // read list order meta-data

        List<String> keys = new LinkedList<String>(record.keySet());
        Collections.sort(keys, new Comparator<String>()
        {
            public int compare(String o1, String o2)
            {
                return Integer.valueOf(o1).compareTo(Integer.valueOf(o2));
            }
        });

        Type defaultType = getCollectionType();
        if (defaultType == null && record.getMeta("type") != null)
        {
            defaultType = typeRegistry.getType(record.getMeta("type"));
        }

        List<Object> instance = instantiate();
        for (String key : keys)
        {
            Object child = record.get(key);
            Type type = defaultType;
            if (child instanceof MutableRecord)
            {
                Record childRecord = (Record) child;
                type = typeRegistry.getType(childRecord.getSymbolicName());
            }
            Object value = type.instantiate(child);
            instance.add(value);
        }
        return instance;
    }

    public List<Object> instantiate() throws TypeConversionException
    {
        return new LinkedList<Object>();
    }

    public void setRecord(String path, Record record, RecordManager recordManager)
    {
        throw new RuntimeException("Method not implemented.");
    }
}
