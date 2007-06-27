package com.zutubi.prototype.type;

import com.zutubi.config.annotations.ID;
import com.zutubi.prototype.config.InstanceCache;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.util.AnnotationUtils;
import com.zutubi.util.Sort;
import com.zutubi.util.logging.Logger;

import java.beans.IntrospectionException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 */
public class MapType extends CollectionType
{
    private static final Logger LOG = Logger.getLogger(MapType.class);

    private String keyProperty;

    public MapType()
    {
        super(HashMap.class);
    }

    @SuppressWarnings({"unchecked"})
    public Map instantiate(String path, InstanceCache cache, Object data) throws TypeException
    {
        Map instance = (Map) (path == null ? null : cache.get(path));
        if (instance == null && data != null)
        {
            if (!(data instanceof Record))
            {
                throw new TypeConversionException("Expected a record, instead received " + data.getClass());
            }

            Record record = (Record) data;

            Type defaultType = getCollectionType();
            instance = new HashMap<String, Object>();
            if (path != null)
            {
                cache.put(path, instance);
            }

            for (String key : record.keySet())
            {
                Object child = record.get(key);
                Type type = defaultType;
                if (child instanceof Record)
                {
                    Record childRecord = (Record) child;
                    type = typeRegistry.getType(childRecord.getSymbolicName());
                    if(type == null)
                    {
                        throw new TypeException("Reference to unrecognised type '" + childRecord.getSymbolicName() + "'");
                    }
                }

                Object value = type.instantiate(path == null ? null : PathUtils.getPath(path, key), cache, child);
                instance.put(key, value);
            }
        }

        return instance;
    }

    @SuppressWarnings({"unchecked"})
    public Object unstantiate(Object instance) throws TypeException
    {
        if(!(instance instanceof Map))
        {
            throw new TypeException("Expecting map, got '" + instance.getClass().getName() + "'");
        }

        MutableRecord result = createNewRecord(true);
        Map<String, Object> map = (Map<String, Object>) instance;
        Type collectionType = getCollectionType();
        for(Map.Entry<String, Object> entry: map.entrySet())
        {
            result.put(entry.getKey(), collectionType.unstantiate(entry.getValue()));
        }

        return result;
    }

    public void setCollectionType(Type collectionType) throws TypeException
    {
        super.setCollectionType(collectionType);

        if(!(collectionType instanceof CompositeType))
        {
            throw new TypeException("Maps may only contain composite types");
        }

        CompositeType compositeType = (CompositeType) collectionType;

        // Unfortunately we cannot use the type registry information as we
        // are part way through registration and cyclical type structures
        // mean that the ID property may not yet have been found.
        try
        {
            keyProperty = AnnotationUtils.getPropertyAnnotatedWith(compositeType.getClazz(), ID.class);
        }
        catch (IntrospectionException e)
        {
            LOG.severe(e);
        }

        if(keyProperty == null)
        {
            throw new TypeException("Types stored in maps must have an @ID property");
        }
    }

    protected Comparator<String> getKeyComparator()
    {
        // Lexicographical ordering by the key.
        return new Sort.StringComparator();
    }

    public Object emptyInstance()
    {
        return new HashMap(0);
    }

    public String getKeyProperty()
    {
        return keyProperty;
    }

    public String getInsertionPath(String path, Record record)
    {
        return PathUtils.getPath(path, (String) record.get(keyProperty));
    }

    public String getSavePath(String path, Record record)
    {
        return PathUtils.getPath(PathUtils.getParentPath(path), (String) record.get(keyProperty));
    }
}
