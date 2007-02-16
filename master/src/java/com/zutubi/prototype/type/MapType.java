package com.zutubi.prototype.type;

import com.zutubi.prototype.type.record.Record;

import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 */
public class MapType extends CollectionType
{
    public MapType(Class type)
    {
        super(type);
    }

    public Object instantiate(Object data) throws TypeException
    {
        Record record = (Record) data;
        if (record == null)
        {
            return null;
        }

        Map instance = new HashMap();

        for (String key : record.keySet())
        {
            Object value = getCollectionType().instantiate(record.get(key));
            instance.put(key, value);
        }
        return instance;
    }

    public Record unstantiate(Object data) throws TypeException
    {
        Record record = new Record();
        Map<String, Object> map = (Map<String, Object>) data;
        for (String key : map.keySet())
        {
            record.put(key, getCollectionType().unstantiate(map.get(key)));
        }
        return record;
    }
}
