package com.zutubi.prototype.config;

import com.zutubi.prototype.type.*;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.RecordManager;

import java.util.*;

/**
 *
 *
 */
public class ConfigurationPersistenceManager
{
    private TypeRegistry typeRegistry;

    private RecordManager recordManager;

    private Map<String, ComplexType> rootScopes = new HashMap<String, ComplexType>();

    /**
     * Register the root scope definitions, from which all of the other definitions will be
     * derived.
     *
     * @param scope name of the scope
     * @param type  type of the object.
     */
    public void register(String scope, ComplexType type)
    {
        rootScopes.put(scope, type);
        recordManager.insert(scope, type.createNewRecord());
    }

    /**
     * Retrieve the type definition for the specified path.
     *
     * @param path
     * @return the type definition, or null if none exists.
     */
    public Type getType(String path)
    {
        String[] pathElements = PathUtils.getPathElements(path);
        String[] parentElements = PathUtils.getParentPathElements(pathElements);
        if (parentElements == null)
        {
            throw new IllegalArgumentException("Invalid path '" + path + "': no parent");
        }

        Record parentRecord = recordManager.load(PathUtils.getPath(parentElements));
        if (parentRecord == null)
        {
            throw new IllegalArgumentException("Invalid path '" + path + "': parent does not exist");
        }

        String lastElement = pathElements[pathElements.length - 1];
        String parentSymbolicName = parentRecord.getSymbolicName();
        Object value = parentRecord.get(lastElement);

        if (parentElements.length == 0)
        {
            // Parent is the base, special case this as the base is currently
            // like a composite without a registered type :/.
            ComplexType type = rootScopes.get(lastElement);
            if (type == null)
            {
                throw new IllegalArgumentException("Invalid path '" + path + ": references non-existant root scope '" + lastElement + "'");
            }

            return type;
        }
        else if (parentSymbolicName == null)
        {
            // Parent is a collection, last segment of path must refer to an
            // existing child composite record.
            if (value == null)
            {
                throw new IllegalArgumentException("Invalid path '" + path + "': references unknown child '" + lastElement + "' of collection");
            }
            // TODO: validate that collections must not contain collections
            return extractRecordType(value, path);
        }
        else
        {
            // Parent is a composite, see if the field exists.
            CompositeType parentType = typeRegistry.getType(parentSymbolicName);
            TypeProperty typeProperty = parentType.getProperty(lastElement);
            if (typeProperty == null)
            {
                throw new IllegalArgumentException("Invalid path '" + path + ": references non-existant field '" + lastElement + "' of type '" + parentSymbolicName + "'");
            }

            Type type = typeProperty.getType();
            if (value == null || type instanceof CollectionType)
            {
                return type;
            }
            else
            {
                // Return the type of the actual value.
                return extractRecordType(value, path);
            }
        }
    }

    private CompositeType extractRecordType(Object value, String path)
    {
        if (!(value instanceof Record))
        {
            throw new IllegalArgumentException("Invalid path '" + path + "': does not reference a complex type");
        }

        Record record = (Record) value;
        return typeRegistry.getType(record.getSymbolicName());
    }

    public <T extends Type> T getType(String path, Class<T> typeClass)
    {
        Type type = getType(path);
        if (type == null)
        {
            throw new IllegalArgumentException("Invalid path '" + path + "': does not exist");
        }

        if (!typeClass.isInstance(type))
        {
            throw new IllegalArgumentException("Invalid path '" + path + "': references incompatible type (expected '" + typeClass.getName() + "', found '" + type.getClass().getName() + "')");
        }

        return (T) type;
    }

    public <T extends Type> T getTargetType(String path, Class<T> typeClass)
    {
        Type type = getTargetType(getType(path, Type.class));
        if (!typeClass.isInstance(type))
        {
            throw new IllegalArgumentException("Invalid path '" + path + "': referenced collection contains incompatible type (expected '" + typeClass.getName() + "', found '" + type.getClass().getName() + "')");
        }

        return (T) type;
    }

    public Type getTargetType(Type type)
    {
        if (type instanceof CollectionType)
        {
            return ((CollectionType) type).getCollectionType();
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
                type = rootScopes.get(pathElement);
                if (type == null)
                {
                    return null;
                }
            }
            else
            {
                if (type instanceof CollectionType)
                {
                    type = ((CollectionType) type).getCollectionType();
                }
                else if (type instanceof CompositeType)
                {
                    type = getProperty((CompositeType) type, pathElement);
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
     * @return object defined by the path.
     * @throws TypeException if there is an type related error loading the instance at the specified path.
     */
    public Object getInstance(String path) throws TypeException
    {
        Record record = recordManager.load(path);
        if (record == null)
        {
            return null;
        }

        CompositeType type = typeRegistry.getType(record.getSymbolicName());
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

    public String insertRecord(String path, Record record)
    {
        ComplexType type = getType(path, ComplexType.class);
        return type.insert(path, record, recordManager);
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

    public void updateRecord(String path, Record record)
    {
        recordManager.store(path, record);
    }
}
