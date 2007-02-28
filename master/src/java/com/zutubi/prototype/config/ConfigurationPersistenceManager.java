package com.zutubi.prototype.config;

import com.zutubi.prototype.annotation.ID;
import com.zutubi.prototype.type.CollectionType;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.ListType;
import com.zutubi.prototype.type.MapType;
import com.zutubi.prototype.type.PrimitiveType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.prototype.type.record.Record;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 *
 *
 */
public class ConfigurationPersistenceManager
{
    private TypeRegistry typeRegistry;

    private RecordManager recordManager;

    private Map<String, Object> rootScopes = new HashMap<String, Object>();

    /**
     * Register the root scope definitions, from which all of the other definitions will be
     * derived.
     *
     * @param scope name of the scope
     * @param obj type of the object.
     */
    public void register(String scope, Object obj)
    {
        rootScopes.put(scope, obj);
    }

    /**
     * Retrieve the type definition for the specified path.
     *
     * @param path
     *
     * @return the type definition, or null if none exists.
     */
    public Type getType(String path)
    {
        Type type = getTypeByRecord(path);
        if (type == null)
        {
            type = getTypeByConfig(path);
        }

        return type;
    }

    public <T extends Type> T getType(String path, Class<T> typeClass)
    {
        Type type = getType(path);
        if(type == null)
        {
            throw new IllegalArgumentException("Invalid path '" + path + "': does not exist");
        }

        if(!typeClass.isInstance(type))
        {
            throw new IllegalArgumentException("Invalid path '" + path + "': references incompatible type (expected '" + typeClass.getName() + "', found '" + type.getClass().getName() + "')");
        }

        return (T)type;
    }

    public <T extends Type> T getTargetType(String path, Class<T> typeClass)
    {
        Type type = getTargetType(getType(path, Type.class));
        if(!typeClass.isInstance(type))
        {
            throw new IllegalArgumentException("Invalid path '" + path + "': referenced collection contains incompatible type (expected '" + typeClass.getName() + "', found '" + type.getClass().getName() + "')");
        }

        return (T) type;
    }

    public Type getTargetType(Type type)
    {
        if(type instanceof CollectionType)
        {
            return ((CollectionType)type).getCollectionType();
        }
        else
        {
            return type;
        }
    }

    protected Type getTypeByConfig(String path)
    {
        Type type = null;
        StringTokenizer tokens = new StringTokenizer(path, "/", false);
        while (tokens.hasMoreTokens())
        {
            String pathElement = tokens.nextToken();
            if (type == null)
            {
                type = (Type) rootScopes.get(pathElement);
                if (type == null)
                {
                    return null;
                }
            }
            else
            {
                if (type instanceof CollectionType)
                {
                    type = ((CollectionType)type).getCollectionType();
                }
                else if(type instanceof CompositeType)
                {
                    type = getProperty((CompositeType)type, pathElement);
                }
                if (type == null)
                {
                    return null;
                }
            }
        }
        return type;
    }

    protected Type getTypeByRecord(String fullPath)
    {
        // Locate the longest path segment with a record and an associated type.
        String path = fullPath;
        CompositeType type = null;
        while (path != null)
        {
            Record record = recordManager.load(path);
            if (record != null)
            {
                type = typeRegistry.getType(record.getSymbolicName());
                if (type != null)
                {
                    break;
                }
            }
            path = getParentPath(path);
        }

        if (type == null)
        {
            return null;
        }

        // then evaluate the remaining path via each types properties.
        String remainingPath = fullPath.substring(path.length());
        StringTokenizer tokens = new StringTokenizer(remainingPath, "/", false);
        while (tokens.hasMoreTokens())
        {
            TypeProperty property = type.getProperty(tokens.nextToken());
            if (property != null && property.getType() instanceof CompositeType)
            {
                type = (CompositeType) property.getType();
            }
        }
        return type;
    }

    public List<String> getListing(String path)
    {
        LinkedList<String> list = new LinkedList<String>();
        Type type = getType(path);
        if (type instanceof CollectionType)
        {
            // load the record
            Record record = recordManager.load(path);
            if (record != null)
            {
                list.addAll(record.keySet());
            }
        }
        else if (type instanceof CompositeType)
        {
            CompositeType compositeType = (CompositeType) type;
            list.addAll(compositeType.getPropertyNames(CompositeType.class));
            list.addAll(compositeType.getPropertyNames(MapType.class));
            list.addAll(compositeType.getPropertyNames(ListType.class));
            return list;
        }
        return list;
    }

    /**
     * Load the object at the specified path, or null if no object exists.
     *
     * @param path
     *
     * @return object defined by the path.
     *
     * @throws TypeException if there is an type related error loading the instance at the specified path.
     */
    public Object getInstance(String path) throws TypeException
    {
        Record record = recordManager.load(path);
        if (record == null)
        {
            return null;
        }

        CompositeType type = (CompositeType) typeRegistry.getType(record.getSymbolicName());
        if (type != null)
        {
            return type.instantiate(record);
        }
        return null;
    }

    public Record getRecord(String path)
    {
        return recordManager.load(path);
    }

    /**
     * Store the object at the specified path.
     *
     * @param path used to identify (and retrieve) the object in future. This path must be consistent with the
     * defined type structure.
     *
     * @see #getType(String)
     *
     * @throws TypeException
     */
/*
    public void setInstance(String path, Object obj) throws TypeException
    {
        Type type = typeRegistry.getType(obj.getClass());
        if (type == null)
        {
            type = typeRegistry.register(obj.getClass());
        }

        // if we are storing an object in the location of an existing collection, add the object to the collection.
        Record existingRecord = recordManager.load(path);
        Type definedType = getType(path);
        if (definedType instanceof CollectionType)
        {
            if (definedType instanceof ListType)
            {
                List<Object> list = (List<Object>) definedType.instantiate(existingRecord);
                if (list == null)
                {
                    list = (List<Object>) definedType.instantiate();
                }
                list.add(obj);
                obj = list;
                type = definedType;
            }
            else if (definedType instanceof MapType)
            {
                Map<String, Object> map = (Map<String, Object>) definedType.instantiate(existingRecord);
                if (map == null)
                {
                    map = (Map<String, Object>) definedType.instantiate();
                }

                map.put(getKey(obj), obj);
                obj = map;
                type = definedType;
            }
        }

        MutableRecord record = (MutableRecord) type.unstantiate(obj);
        recordManager.store(path, record);
    }
*/

   public void setRecord(String path, Record record)
   {
       Type type = getType(path);
       type.setRecord(path, record, recordManager);
   }

    public String getKey(Object obj) throws TypeException
    {
        try
        {
            TypeProperty keyProperty = getKeyProperty(obj);
            if (keyProperty != null)
            {
                return keyProperty.getGetter().invoke(obj).toString();
            }
            return obj.toString();
        }
        catch (Exception e)
        {
            throw new TypeException(e);
        }
    }

    public TypeProperty getKeyProperty(Object obj)
    {
        CompositeType type = (CompositeType) typeRegistry.getType(obj.getClass());
        for (TypeProperty property : type.getProperties(PrimitiveType.class))
        {
            if (property.getAnnotation(ID.class) != null)
            {
                return property;
            }
        }
        return null;
    }

    public String getParentPath(String path)
    {
        if (path.indexOf("/") != -1)
        {
            return path.substring(0, path.lastIndexOf("/"));
        }
        return null;
    }

    private Type getProperty(CompositeType type, String path)
    {
        TypeProperty property = type.getProperty(path);
        if (property != null)
        {
            return property.getType();
        }
        return null;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}
