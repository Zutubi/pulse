package com.zutubi.prototype.type;

import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.prototype.type.record.MutableRecord;
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
    private static final String LATEST_KEY_KEY = "latestKey";

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
                String[] references = (String[]) data;
                for (String reference : references)
                {
                    instance.add(type.instantiate(path, reference));
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

    public Object unstantiate(Object instance) throws TypeException
    {
        if(!(instance instanceof List))
        {
            throw new TypeException("Expecting list, got '" + instance.getClass().getName() + "'");
        }

        List list = (List) instance;
        Type collectionType = getCollectionType();
        if(collectionType instanceof SimpleType)
        {
            // Make it a String array
            String[] result = new String[list.size()];
            int i = 0;
            for(Object o: list)
            {
                result[i++] = (String) collectionType.unstantiate(o);
            }

            return result;
        }
        else
        {
            // A sub-record.  We have no sensible way to define an ordering...
            MutableRecord result = createNewRecord();
            int i = 0;
            for(Object o: list)
            {
                result.put(Integer.toString(i++), collectionType.unstantiate(o));
            }

            return result;
        }
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

    protected String getItemKey(String path, Record collectionRecord, Record itemRecord, RecordManager recordManager)
    {
        String latestKey = collectionRecord.getMeta(LATEST_KEY_KEY);
        if (latestKey == null)
        {
            latestKey = "1";
        }
        else
        {
            latestKey = Integer.toString(Integer.parseInt(latestKey) + 1);
        }

        MutableRecord mutableRecord = collectionRecord.copy(false);
        mutableRecord.putMeta(LATEST_KEY_KEY, latestKey);
        recordManager.update(path, mutableRecord);
        return latestKey;
    }

    public String save(String path, String baseName, Record record, RecordManager recordManager)
    {
        String newPath = PathUtils.getPath(path, baseName);
        recordManager.update(newPath, record);
        return newPath;
    }
}
