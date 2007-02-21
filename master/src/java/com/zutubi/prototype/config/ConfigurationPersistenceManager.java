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
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.RecordManager;

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
    public Object getType(String path)
    {
        Object type = getTypeByRecord(path);
        if (type == null)
        {
            type = getTypeByConfig(path);
        }

        return type;
    }

    protected Object getTypeByConfig(String path)
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
                    return null;
                }
                type = getProperty(type, pathElement);
                if (type == null)
                {
                    return null;
                }
            }
        }
        return type;
    }

    protected Object getTypeByRecord(String fullPath)
    {
        // a) locate the longest path segment with a record and an associated type.
        String path = fullPath;
        Type type = null;
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

        String remainingPath = fullPath.substring(path.length());
        StringTokenizer tokens = new StringTokenizer(remainingPath, "/", false);
        while (tokens.hasMoreTokens())
        {
            TypeProperty property = ((CompositeType)type).getProperty(tokens.nextToken());
            if (property != null)
            {
                type = property.getType();
            }
        }

        return type;
    }

    public List<String> getListing(String path)
    {
        LinkedList<String> list = new LinkedList<String>();
        Object type = getType(path);
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
            CompositeType ctype = (CompositeType) type;
            list.addAll(ctype.getProperties(CompositeType.class));
            list.addAll(ctype.getProperties(MapType.class));
            list.addAll(ctype.getProperties(ListType.class));
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
     */
    public Object getInstance(String path) throws TypeException
    {
        Record record = recordManager.load(path);
        if (record == null)
        {
            return null;
        }

        Type type = (Type) getType(path);
        if (type != null)
        {
            return type.instantiate(record);
        }
        return null;
    }

    /**
     * Store the object at the specified path.
     *
     * @param path used to identify (and retrieve) the object in future. This path must be consistent with the
     * defined type structure.
     *
     * @param obj instance to persisted.
     *
     * @see #getType(String) 
     */
    public void setInstance(String path, Object obj) throws TypeException
    {
        Type type = typeRegistry.getType(obj.getClass());
        if (type == null)
        {
            type = typeRegistry.register(obj.getClass());
        }

        // if we are storing an object in the location of an existing collection, add the object to the collection.
        Record existingRecord = recordManager.load(path);
        Type definedType = (Type) getType(path);
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

        Record record = (Record) type.unstantiate(obj);
        recordManager.store(path, record);
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
        Type type = typeRegistry.getType(obj.getClass());
        if (type instanceof CompositeType)
        {
            CompositeType ctype = (CompositeType) type;
            for (String propertyName : ctype.getProperties(PrimitiveType.class))
            {
                TypeProperty property = ctype.getProperty(propertyName);
                if (property.getAnnotation(ID.class) != null)
                {
                    return property;
                }
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

    private Type getProperty(Type type, String path)
    {
        CompositeType ctype = (CompositeType) type;
        TypeProperty property = ctype.getProperty(path);
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
