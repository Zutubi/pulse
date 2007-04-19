package com.zutubi.prototype.config;

import com.zutubi.prototype.annotation.Reference;
import com.zutubi.prototype.config.events.*;
import com.zutubi.prototype.type.*;
import com.zutubi.prototype.type.record.*;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.logging.Logger;
import com.zutubi.validation.i18n.MessagesTextProvider;
import com.zutubi.validation.ValidationAware;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.ValidationManager;

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
    private ValidationManager validationManager;
    private ObjectFactory objectFactory;
    private EventManager eventManager;

    private Map<String, ScopeInfo> rootScopes = new HashMap<String, ScopeInfo>();
    /**
     * An index mapping from composite types to all paths where records of that
     * type (or a subtype) may reside.  Paths will include wildcards to navigate
     * collection members.
     */
    private Map<CompositeType, List<String>> compositeTypePathIndex = new HashMap<CompositeType, List<String>>();
    private InstanceCache instances = new InstanceCache();
    private Map<String, List<String>> references = new HashMap<String, List<String>>();

    public void init()
    {
        refreshInstances();
    }

    /**
     * Register the root scope definitions, from which all of the other definitions will be
     * derived.
     *
     * @param scope name of the scope
     * @param type  type of the object.
     */
    public void register(String scope, ComplexType type)
    {
        rootScopes.put(scope, new ScopeInfo(scope, type));
        if (!recordManager.containsRecord(scope))
        {
            recordManager.insert(scope, type.createNewRecord());
        }

        if (type instanceof CompositeType)
        {
            updateIndex(scope, (CompositeType) type);
        }
        else
        {
            if (type instanceof CollectionType)
            {
                updateIndex(scope, (CollectionType) type);
            }
        }
    }

    private void updateIndex(String path, CompositeType type)
    {
        // Add an entry at the current path, and analyse properties
        addToIndex(type, path);
        for (TypeProperty property : type.getProperties(CompositeType.class))
        {
            String childPath = PathUtils.getPath(path, property.getName());
            updateIndex(childPath, (CompositeType) property.getType());
        }
        for (TypeProperty property : type.getProperties(CollectionType.class))
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
        if (targetType instanceof CompositeType)
        {
            updateIndex(PathUtils.getPath(path, PathUtils.WILDCARD_ANY_ELEMENT), (CompositeType) targetType);
        }
    }

    private void addToIndex(CompositeType composite, String path)
    {
        ensureConfigurationPaths(composite).add(path);
    }

    private List<String> ensureConfigurationPaths(CompositeType type)
    {
        List<String> l = compositeTypePathIndex.get(type);
        if (l == null)
        {
            l = new ArrayList<String>();
            compositeTypePathIndex.put(type, l);
        }
        return l;
    }

    public Map<String, Record> getReferencableRecords(CompositeType type, String referencingPath)
    {
        HashMap<String, Record> records = new HashMap<String, Record>();
        // FIXME does not account for templating, and may need to be more
        // FIXME general.  review when we have more config objects...
        for (String path : getOwningPaths(type, getClosestOwningScope(type, referencingPath)))
        {
            recordManager.loadAll(path, records);
        }

        return records;
    }

    String getClosestOwningScope(CompositeType type, String path)
    {
        List<String> paths = compositeTypePathIndex.get(type);
        if (paths != null)
        {
            // Find the closest by starting at our path and working up the
            // ancestry until one hits.
            path = PathUtils.normalizePath(path);
            while (path != null)
            {
                for (String candidate : paths)
                {
                    if (PathUtils.prefixMatches(candidate, path))
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
        if (prefix != null && paths != null)
        {
            for (String owningPath : paths)
            {
                if (PathUtils.prefixMatches(owningPath, prefix))
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
            ScopeInfo info = rootScopes.get(lastElement);
            if (info == null)
            {
                throw new IllegalArgumentException("Invalid path '" + path + ": references non-existant root scope '" + lastElement + "'");
            }

            return info.getType();
        }
        else
        {
            if (parentSymbolicName == null)
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
        if(path.length() == 0)
        {
            // Root listing
            list.addAll(rootScopes.keySet());
        }
        else
        {
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
            else
            {
                if (type instanceof CompositeType)
                {
                    CompositeType compositeType = (CompositeType) type;
                    list.addAll(compositeType.getPropertyNames(CompositeType.class));
                    list.addAll(compositeType.getPropertyNames(MapType.class));
                    list.addAll(compositeType.getPropertyNames(ListType.class));
                    return list;
                }
            }
        }
        return list;
    }

    public List<String> getConfigurationPaths(CompositeType type)
    {
        return compositeTypePathIndex.get(type);
    }

    private void refreshInstances()
    {
        instances.clear();
        references.clear();

        for (ScopeInfo scope : rootScopes.values())
        {
            String path = scope.getScopeName();
            Type type = scope.getType();
            try
            {
                type.instantiate(path, recordManager.load(path));
            }
            catch (TypeException e)
            {
                // FIXME how should we present this? i think we need to
                // FIXME store and allow the UI to show such problems
                LOG.severe("Unable to instantiate '" + path + "': " + e.getMessage(), e);
            }
        }
    }

    /**
     * Load the object at the specified path, or null if no object exists.
     *
     * @param path path of the instance to retrieve
     * @return object defined by the path.
     */
    public Object getInstance(String path)
    {
        return instances.get(path);
    }

    public <T> T getInstance(String path, Class<T> clazz)
    {
        Object instance = getInstance(path);
        if(instance == null)
        {
            return null;
        }

        if(!clazz.isAssignableFrom(instance.getClass()))
        {
            throw new IllegalArgumentException("Path '" + path + "' does not reference an instance of type '" + clazz.getName() + "'");
        }

        return (T)instance;
    }

    public <T> Collection<T> getAllInstances(Class<T> clazz)
    {
        CompositeType type = typeRegistry.getType(clazz);
        if(type == null)
        {
            return Collections.EMPTY_LIST;
        }

        List<T> result = new LinkedList<T>();
        List<String> paths = compositeTypePathIndex.get(type);
        if (paths != null)
        {
            for(String path: paths)
            {
                instances.getAll(path, result);
            }
        }

        return result;
    }

    public Object resolveReference(String fromPath, String toPath) throws TypeException
    {
        indexReference(fromPath, toPath);
        Object instance = getInstance(toPath);
        if (instance == null)
        {
            Record record = getRecord(toPath);
            if (record == null || record.getSymbolicName() == null)
            {
                throw new TypeException("Broken reference from '" + fromPath + "' to '" + toPath + "'");
            }

            Type type = typeRegistry.getType(record.getSymbolicName());
            if (type == null)
            {
                throw new TypeException("Reference to unrecognised type '" + record.getSymbolicName() + "'");
            }

            instance = type.instantiate(toPath, record);
        }

        return instance;
    }

    private void indexReference(String fromPath, String toPath)
    {
        List<String> index = references.get(toPath);
        if (index == null)
        {
            index = new LinkedList<String>();
            references.put(toPath, index);
        }

        index.add(fromPath);
    }

    public void putInstance(String path, Object instance)
    {
        instances.put(path, instance);
    }

    public Record getRecord(String path)
    {
        Record record = recordManager.load(path);
        if (record == null)
        {
            return null;
        }
        return templatiseRecord(path, record);
    }

    private void getAllRecords(String path, Map<String, Record> records)
    {
        recordManager.loadAll(path, records);
        for (Map.Entry<String, Record> entry : records.entrySet())
        {
            entry.setValue(templatiseRecord(entry.getKey(), entry.getValue()));
        }
    }

    private Record templatiseRecord(String path, Record record)
    {
        // We need to understand the root level can be templated.
        String[] pathElements = PathUtils.getPathElements(path);
        if (pathElements.length > 1)
        {
            ScopeInfo scopeInfo = rootScopes.get(pathElements[0]);
            if (scopeInfo.isTemplated())
            {
                // Need to load a chain of templates.
                // FIXME this does not appear to handle the case where the
                // FIXME parent has no configuration but the grandparent does
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

    public String insertRecord(final String path, Record record)
    {
        ComplexType type = getType(path, ComplexType.class);

        eventManager.publish(new PreInsertEvent(this, path));

        final String result = type.insert(path, record, recordManager);
        refreshInstances();

        eventManager.publish(new PostInsertEvent(this, path, result, instances.get(result)));

        return result;
    }

    public boolean validate(String parentPath, String baseName, Record subject, ValidationAware validationCallback) throws TypeException
    {
        // The type we validating against.
        Type type = typeRegistry.getType(subject.getSymbolicName());

        // Construct the validation context, wrapping it around the validation callback to that the
        // client is notified of validation errors.
        MessagesTextProvider textProvider = new MessagesTextProvider(type.getClazz());
        ValidationContext context = new ConfigurationValidationContext(validationCallback, textProvider, parentPath == null ? null : getInstance(parentPath), baseName);

        // Create an instance of the object represented by the record.  It is during the instantiation that
        // type conversion errors are detected.
        Object instance;
        try
        {
            instance = type.instantiate(null, subject);
        }
        catch (TypeConversionException e)
        {
            for (String field : e.getFieldErrors())
            {
                context.addFieldError(field, e.getFieldError(field));
            }
            return false;
        }

        // Process the instance via the validation manager.
        try
        {
            validationManager.validate(instance, context);
            return !context.hasErrors();
        }
        catch (ValidationException e)
        {
            context.addActionError(e.getMessage());
            return false;
        }
    }

    public String saveRecord(String parentPath, String baseName, Record record)
    {
        Record parentRecord = recordManager.load(parentPath);
        if (parentRecord == null)
        {
            throw new IllegalArgumentException("Invalid parent path '" + parentPath + "'");
        }

        ComplexType parentType = (ComplexType) getType(parentPath);

        String path = PathUtils.getPath(parentPath, baseName);
        Object oldInstance = instances.get(path);
        eventManager.publish(new PreSaveEvent(this, path, oldInstance));

        final String newPath = parentType.save(parentPath, baseName, record, recordManager);
        refreshInstances();

        eventManager.publish(new PostSaveEvent(this, path, oldInstance, newPath, instances.get(newPath)));

        return newPath;
    }

    public void renameReferences(final String oldPath, final String newPath)
    {
        List<String> index = references.get(oldPath);
        if (index != null)
        {
            for (String referencingPath : index)
            {
                String parentPath = PathUtils.getParentPath(referencingPath);
                String baseName = PathUtils.getBaseName(referencingPath);

                Record parentRecord = recordManager.load(parentPath);
                if (parentRecord != null)
                {
                    Object currentValue = parentRecord.get(baseName);
                    if (currentValue != null)
                    {
                        MutableRecord newRecord = parentRecord.copy(false);
                        Object newValue = currentValue;

                        if (currentValue instanceof String)
                        {
                            newValue = newPath;
                        }
                        else if (currentValue instanceof String[])
                        {
                            String[] refs = (String[]) currentValue;
                            newValue = CollectionUtils.mapToArray(refs, new Mapping<String, String>()
                            {
                                public String map(String s)
                                {
                                    if (s.equals(oldPath))
                                    {
                                        return newPath;
                                    }
                                    else
                                    {
                                        return s;
                                    }
                                }
                            }, new String[refs.length]);
                        }

                        newRecord.put(baseName, newValue);
                        recordManager.update(parentPath, newRecord);
                    }
                }
            }
        }
    }

    public ReferenceCleanupTask getCleanupTasks(String path)
    {
        DeleteReferenceCleanupTask result = new DeleteReferenceCleanupTask(path, recordManager);
        List<String> index = references.get(path);
        if(index != null)
        {
            for(String referencingPath: index)
            {
                ReferenceCleanupTaskProvider provider = getCleanupTaskProvider(referencingPath);
                if (provider != null)
                {
                    ReferenceCleanupTask task = provider.getAction(path, referencingPath);
                    if(task != null)
                    {
                        result.addCascaded(task);
                    }
                }
            }
        }

        return result;
    }

    private ReferenceCleanupTaskProvider getCleanupTaskProvider(String referencingPath)
    {
        String parentPath = PathUtils.getParentPath(referencingPath);
        String baseName = PathUtils.getBaseName(referencingPath);

        CompositeType parentType = (CompositeType) getType(parentPath);
        TypeProperty property = parentType.getProperty(baseName);
        Reference ref = property.getAnnotation(Reference.class);
        Class<? extends ReferenceCleanupTaskProvider> providerClass = ref.cleanupTaskProvider();
        try
        {
            return objectFactory.buildBean(providerClass);
        }
        catch (Exception e)
        {
            LOG.severe("Unable to instantiate reference cleanup task provider of class '" + ref.cleanupTaskProvider().getName() + "': " + e.getMessage(), e);
            try
            {
                return objectFactory.buildBean(DefaultReferenceCleanupTaskProvider.class);
            }
            catch (Exception omg)
            {
                LOG.severe("Unable to instantiate default reference cleanup task provider: " + e.getMessage(), e);
                return null;
            }
        }
    }

    public void delete(final String path)
    {
        Object oldInstance = instances.get(path);
        eventManager.publish(new PreDeleteEvent(this, path, oldInstance));

        getCleanupTasks(path).execute();
        refreshInstances();

        eventManager.publish(new PostDeleteEvent(this, path, oldInstance));
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }

    public void setValidationManager(ValidationManager validationManager)
    {
        this.validationManager = validationManager;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    /**
     * Holds information about a root scope.
     */
    private class ScopeInfo
    {
        private String scopeName;
        private ComplexType type;

        public ScopeInfo(String scopeName, ComplexType type)
        {
            this.scopeName = scopeName;
            this.type = type;
        }

        public String getScopeName()
        {
            return scopeName;
        }

        public ComplexType getType()
        {
            return type;
        }

        public Type getTargetType()
        {
            return type.getTargetType();
        }

        public boolean isCollection()
        {
            return type instanceof CollectionType;
        }

        public boolean isTemplated()
        {
            return type.isTemplated();
        }
    }
}
