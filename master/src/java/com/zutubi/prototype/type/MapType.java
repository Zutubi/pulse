package com.zutubi.prototype.type;

import com.zutubi.prototype.annotation.ID;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.config.ConfigurationPersistenceManager;

import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 */
public class MapType extends CollectionType
{
    private ConfigurationPersistenceManager configurationPersistenceManager;

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
                        throw new TypeException("Reference to unrecorgnised type '" + childRecord.getSymbolicName() + "'");
                    }
                }

                Object value = type.instantiate(path == null ? null : PathUtils.getPath(path, key), child);
                instance.put(key, value);
            }
        }

        return instance;
    }

    public TypeProperty getKeyProperty(Object obj)
    {
        // TODO: assumes a Map only holds composites, which is fair enough but
        // would need to be enforced at registration time.
        CompositeType type = (CompositeType) getCollectionType();
        for (TypeProperty property : type.getProperties(PrimitiveType.class))
        {
            if (property.getAnnotation(ID.class) != null)
            {
                return property;
            }
        }
        return null;
    }
}
