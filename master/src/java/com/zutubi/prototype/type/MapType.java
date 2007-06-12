package com.zutubi.prototype.type;

import com.zutubi.config.annotations.ID;
import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.util.AnnotationUtils;
import com.zutubi.util.logging.Logger;

import java.beans.IntrospectionException;
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
    private ConfigurationTemplateManager configurationTemplateManager;

    public MapType(ConfigurationTemplateManager configurationTemplateManager)
    {
        super(HashMap.class);
        this.configurationTemplateManager = configurationTemplateManager;
    }

    @SuppressWarnings({"unchecked"})
    public Map instantiate(String path, Object data) throws TypeException
    {
        Map instance = (Map) (path == null ? null : configurationTemplateManager.getInstance(path));
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
                configurationTemplateManager.putInstance(path, instance);
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

                Object value = type.instantiate(path == null ? null : PathUtils.getPath(path, key), child);
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

        MutableRecord result = createNewRecord();
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

    public String getKeyProperty()
    {
        return keyProperty;
    }

    protected String getItemKey(String path, Record collectionRecord, Record itemRecord, RecordManager recordManager)
    {
        return (String) itemRecord.get(keyProperty);
    }

    public String save(String path, String baseName, Record record, RecordManager recordManager)
    {
        // Check for renames
        String newName = (String) record.get(keyProperty);
        if(baseName != null && !baseName.equals(newName))
        {
            // We need to update our own record
            String oldPath = PathUtils.getPath(path, baseName);
            String newPath = PathUtils.getPath(path, newName);

            recordManager.move(oldPath, newPath);
            recordManager.update(newPath, record);
            return newPath;
        }
        else
        {
            // Regular update.
            String newPath = PathUtils.getPath(path, baseName);
            recordManager.update(newPath, record);
            return newPath;
        }
    }
}
