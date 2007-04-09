package com.zutubi.prototype.config;

import com.zutubi.prototype.type.*;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.prototype.type.record.TemplateRecord;
import com.zutubi.pulse.util.logging.Logger;

import java.util.*;

/**
 *
 *
 */
@SuppressWarnings({"unchecked"})
public class ConfigurationPersistenceManager
{
    private static final Logger LOG = Logger.getLogger(ConfigurationPersistenceManager.class);
    
    private TypeRegistry typeRegistry;

    private RecordManager recordManager;

    private Map<String, ComplexType> rootScopes = new HashMap<String, ComplexType>();
    /**
     * An index mapping from composite types to all paths where records of that
     * type (or a subtype) may reside.  Paths will include wildcards to navigate
     * collection members.
     */
    private Map<CompositeType, List<String>> compositeTypePathIndex = new HashMap<CompositeType, List<String>>();

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
        if(!recordManager.containsRecord(scope))
        {
            recordManager.insert(scope, type.createNewRecord());
        }

        if(type instanceof CompositeType)
        {
            updateIndex(scope, (CompositeType) type);
        }
        else if(type instanceof CollectionType)
        {
            updateIndex(scope, (CollectionType) type);
        }
    }

    private void updateIndex(String path, CompositeType type)
    {
        // Add an entry at the current path, and analyse properties
        addToIndex(type, path);
        for(TypeProperty property: type.getProperties(CompositeType.class))
        {
            String childPath = PathUtils.getPath(path, property.getName());
            updateIndex(childPath, (CompositeType) property.getType());
        }
        for(TypeProperty property: type.getProperties(CollectionType.class))
        {
            String childPath = PathUtils.getPath(path, property.getName());
            updateIndex(childPath, (CollectionType) property.getType());
        }
    }

    private void updateIndex(String path, CollectionType type)
    {
        // If the collection itself holds a complex type, add a wildcard
        // to the path and traverse down.
        Type targetType = type.getCollectionType();
        if(targetType instanceof CompositeType)
        {
            updateIndex(PathUtils.getPath(path, PathUtils.WILDCARD_ANY_ELEMENT), (CompositeType) targetType);
        }
    }

    private void addToIndex(CompositeType composite, String path)
    {
        getConfigurationPaths(composite).add(path);
    }

    List<String> getConfigurationPaths(CompositeType type)
    {
        List<String> l = compositeTypePathIndex.get(type);
        if(l == null)
        {
            l = new ArrayList<String>();
            compositeTypePathIndex.put(type, l);
        }
        return l;
    }

    public Map<String, Record> getReferencableRecords(CompositeType type, String referencingPath)
    {
        HashMap<String, Record> records = new HashMap<String, Record>();
        for(String path: getOwningPaths(type, getClosestOwningScope(type, referencingPath)))
        {
            recordManager.loadAll(path, records);
        }

        return records;
    }

    String getClosestOwningScope(CompositeType type, String path)
    {
        List<String> paths = compositeTypePathIndex.get(type);
        if(paths != null)
        {
            // Find the closest by starting at our path and working up the
            // ancestry until one hits.
            path = PathUtils.normalizePath(path);
            while(path != null)
            {
                for(String candidate: paths)
                {
                    if(PathUtils.prefixMatches(candidate, path))
                    {
                        return PathUtils.getParentPath(path);
                    }
                }

                path = PathUtils.getParentPath(path);
            }
        }

        return null;
    }

    List<String> getOwningPaths(CompositeType type, String prefix)
    {
        List<String> paths = compositeTypePathIndex.get(type);
        List<String> result = new LinkedList<String>();
        if(prefix != null && paths != null)
        {
            for(String owningPath: paths)
            {
                if(PathUtils.prefixMatches(owningPath, prefix))
                {
                    result.add(PathUtils.getPath(prefix, PathUtils.stripMatchingPrefix(owningPath, prefix)));
                }
            }
        }

        return result;
    }

    /**
     * Retrieve the type definition for the specified path.
     *
     * @param path the path to retrieve the type of
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
     * @param path path of the instance to retrieve
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
        Record record = recordManager.load(path);
        if (record == null)
        {
            return null;
        }

        // We need to understand the root level can be templated.
        String[] pathElements = PathUtils.getPathElements(path);
        if (pathElements.length > 1)
        {
            ComplexType scopeType = rootScopes.get(pathElements[0]);
            if (scopeType.isTemplated())
            {
                // Need to load a chain of templates.
                String owner = pathElements[1];
                Record owningRecord = recordManager.load(PathUtils.getPath(pathElements[0], pathElements[1]));
                String parent = owningRecord.getMeta("parent");
                TemplateRecord parentRecord = null;
                if (parent != null)
                {
                    pathElements[1] = parent;
                    parentRecord = (TemplateRecord) getRecord(PathUtils.getPath(pathElements));
                }

                return new TemplateRecord(owner, parentRecord, record);
            }
        }
        return record;
    }

    public String insertRecord(String path, Record record)
    {
        ComplexType type = getType(path, ComplexType.class);
        return type.insert(path, record, recordManager);
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }

    public void saveRecord(String path, Record record)
    {
        recordManager.insertOrUpdate(path, record);
    }
}
