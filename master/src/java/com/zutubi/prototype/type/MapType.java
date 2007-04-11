package com.zutubi.prototype.type;

import com.zutubi.prototype.annotation.ID;
import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.RecordManager;

import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 */
public class MapType extends CollectionType
{
    private ConfigurationPersistenceManager configurationPersistenceManager;
    private String keyProperty;

    public MapType(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        super(HashMap.class);
        this.configurationPersistenceManager = configurationPersistenceManager;
    }

    public Map instantiate(String path, Object data) throws TypeException
    {
        Map instance = (Map) (path == null ? null : configurationPersistenceManager.getInstance(path));
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
                configurationPersistenceManager.putInstance(path, instance);
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
                //noinspection unchecked
                instance.put(key, value);
            }
        }

        return instance;
    }

    public void setCollectionType(Type collectionType) throws TypeException
    {
        super.setCollectionType(collectionType);

        if(!(collectionType instanceof CompositeType))
        {
            throw new TypeException("Maps may only contain composite types");
        }

        CompositeType compositeType = (CompositeType) collectionType;
        for (TypeProperty property : compositeType.getProperties(PrimitiveType.class))
        {
            if (property.getAnnotation(ID.class) != null)
            {
                keyProperty = property.getName();
                break;
            }
        }

        // FIXME: barfs on cycles
//        if(keyProperty == null)
//        {
//            throw new TypeException("Types stored in maps must have an @ID property");
//        }
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
            // We need to update our own record and tell the CPM to update
            // references.
            String oldPath = PathUtils.getPath(path, baseName);
            String newPath = PathUtils.getPath(path, newName);

            // Rename references first, as the below changes to the record
            // will invalidate the reference index.
            configurationPersistenceManager.renameReferences(oldPath, newPath);

            recordManager.copy(oldPath, newPath);
            recordManager.delete(oldPath);
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
