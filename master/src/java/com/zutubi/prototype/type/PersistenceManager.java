package com.zutubi.prototype.type;

import com.zutubi.prototype.config.ConfigurationRegistry;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.RecordManager;

import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class PersistenceManager
{
    private RecordManager recordManager;
    private TypeRegistry typeRegistry;
    private ConfigurationRegistry configurationRegistry;

    public void store(String path, Object obj) throws TypeException
    {
        Class<? extends Object> objectType = obj.getClass();
        Type type = typeRegistry.getType(objectType);
        if (type == null)
        {
            throw new IllegalArgumentException("Unsupported object type: " + objectType);
        }

        // if we are storing an object in the location of an existing collection, add the object to the collection.
        Record existingRecord = recordManager.load(path);
        if (isCollection(path))
        {
            Type collectionType = configurationRegistry.getType(path);
            if (collectionType instanceof ListType)
            {
                List<Object> list = (List<Object>) collectionType.instantiate(existingRecord);
                if (list == null)
                {
                    list = (List<Object>) collectionType.instantiate();
                }
                list.add(obj);
                obj = list;
                type = collectionType;
            }
            else if (collectionType instanceof MapType)
            {
                Map<String, Object> map = (Map<String, Object>) collectionType.instantiate(existingRecord);
                if (map == null)
                {
                    map = (Map<String, Object>) collectionType.instantiate();
                }
                //TODO: need a key property on the object.
                map.put(obj.toString(), obj);
                obj = map;
                type = collectionType;
            }
        }

        Record record = (Record) type.unstantiate(obj);
        recordManager.store(path, record);
    }

    public Object load(String path) throws TypeException
    {
        Record record = recordManager.load(path);
        if (record != null)
        {
            Type type = typeRegistry.getType(record.getSymbolicName());
            return type.instantiate(record);
        }
        return null;
    }

    public Type getType(String path)
    {
        Record record = recordManager.load(path);
        if (record != null)
        {
            return typeRegistry.getType(record.getSymbolicName());
        }
        return configurationRegistry.getType(path);
    }

    public void delete(String path)
    {
        recordManager.delete(path);        
    }

    public boolean isCollection(String path)
    {
        Type type = configurationRegistry.getType(path);
        if (type != null)
        {
            return (type instanceof ListType) | (type instanceof MapType);
        }
        return false;
    }

    /**
     * Persistence convenience method that takes a data map and stores it in the specified persistent location.
     *
     * @param path the path at which the data is to be stored.
     * @param symbolicName the symbolic name defining the type of the data.
     * @param parameters the data to be stored.
     *
     * @throws TypeException if there are any problems storing the requested type at the
     * specified location.
     */
    public void saveToStore(String path, String symbolicName, Map<String, Object> parameters) throws TypeException
    {
        CompositeType type = (CompositeType) typeRegistry.getType(symbolicName);

        // load existing instance if it exists, else generate a temporary instance.
        Object instance = null;
        if (!isCollection(path))
        {
            // augment existing type if it is there.
            instance = load(path);
        }

        // apply parameters to instance.
        if (instance != null)
        {
            type.populateInstance(parameters, instance);
        }
        else
        {
            instance = type.instantiate(parameters);
        }

        // validate.

        store(path, instance);
    }

    public void saveToStore(String path, Object instance) throws TypeException
    {
        // validate?

        store(path, instance);
    }

    public Object saveToInstance(Map<String, Object> parameters, Object instance) throws TypeException
    {
        CompositeType type = (CompositeType) typeRegistry.getType(instance.getClass());
        if (type == null)
        {
            type = typeRegistry.registerAnonymous(instance.getClass());
        }
        type.populateInstance(parameters, instance);
        return instance;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setConfigurationRegistry(ConfigurationRegistry configurationRegistry)
    {
        this.configurationRegistry = configurationRegistry;
    }
}
