package com.zutubi.prototype.type;

import com.zutubi.prototype.type.record.Record;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 *
 */
public class ListType extends CollectionType
{
    public ListType(Class type)
    {
        super(type);
    }

    public List<Object> instantiate(Object data) throws TypeException
    {
        Map<String, Object> record = (Map<String, Object>) data;
        if (record == null)
        {
            return null;
        }

        List<String> keys = new LinkedList<String>(record.keySet());
        Collections.sort(keys, new Comparator<String>()
        {
            public int compare(String o1, String o2)
            {
                return Integer.valueOf(o1).compareTo(Integer.valueOf(o2));
            }
        });

        List<Object> instance = new LinkedList<Object>();
        for (String key : keys)
        {
            Object value = getCollectionType().instantiate(record.get(key));
            instance.add(value);
        }

        return instance;
    }

    public Record unstantiate(Object data) throws TypeException
    {
        Record record = new Record();

        List<Object> list = (List<Object>) data;
        for (int i = 0; i < list.size(); i++)
        {
            Object obj = list.get(i);
            record.put(String.valueOf(i), getCollectionType().unstantiate(obj));
        }

        return record;
    }
}
