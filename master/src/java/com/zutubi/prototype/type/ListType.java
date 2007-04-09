package com.zutubi.prototype.type;

import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.config.ConfigurationPersistenceManager;

import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class ListType extends CollectionType
{
    private ConfigurationPersistenceManager configurationPersistenceManager;

    public ListType(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        super(LinkedList.class);
        this.configurationPersistenceManager = configurationPersistenceManager;
    }

    public List<Object> instantiate(String path, Object data) throws TypeException
    {
        List<Object> instance = (List<Object>) (path == null ? null : configurationPersistenceManager.getInstance(path));
        if (instance == null && data != null)
        {
            if (data instanceof Record)
            {
                Record record = (Record) data;

                instance = create(path);

                Iterable<String> keys = getOrder(record);
                Type defaultType = getCollectionType();
                for (String key : keys)
                {
                    Object child = record.get(key);
                    Type type = defaultType;
                    if (child instanceof Record)
                    {
                        Record childRecord = (Record) child;
                        String symbolicName = childRecord.getSymbolicName();
                        type = typeRegistry.getType(symbolicName);
                        if(type == null)
                        {
                            throw new TypeException("Reference to unknown type '" + symbolicName + "'");
                        }
                    }
                    Object value = type.instantiate(path == null ? null : PathUtils.getPath(path, key), child);
                    instance.add(value);
                }

                return instance;
            }
            else if(data instanceof String[])
            {
                instance = create(path);
                Type type = getCollectionType();
                for(String s: (String[])data)
                {
                    instance.add(type.instantiate(path == null ? null : PathUtils.getPath(path, s), s));
                }
                return instance;
            }
            else
            {
                throw new TypeConversionException("Expected a record or string array, instead received " + data.getClass());
            }
        }

        return instance;
    }

    private List<Object> create(String path)
    {
        List<Object> instance;
        instance = new LinkedList<Object>();
        if (path != null)
        {
            configurationPersistenceManager.putInstance(path, instance);
        }
        return instance;
    }
}
