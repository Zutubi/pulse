package com.zutubi.prototype.config;

import com.zutubi.prototype.config.events.*;
import com.zutubi.prototype.type.*;
import com.zutubi.prototype.type.record.*;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.util.logging.Logger;
import com.zutubi.validation.ValidationAware;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.ValidationManager;
import com.zutubi.validation.i18n.MessagesTextProvider;

import java.util.*;

/**
 */
public class ConfigurationTemplateManager
{
    private static final Logger LOG = Logger.getLogger(ConfigurationTemplateManager.class);

    private static final String PARENT_KEY = "parentHandle";
    private static final String TEMPLATE_KEY = "template";

    private InstanceCache instances = new InstanceCache();
    private Map<String, TemplateHierarchy> templateHierarchies = new HashMap<String, TemplateHierarchy>();

    private TypeRegistry typeRegistry;
    private RecordManager recordManager;
    private ConfigurationPersistenceManager configurationPersistenceManager;
    private ConfigurationReferenceManager configurationReferenceManager;
    private EventManager eventManager;
    private ValidationManager validationManager;

    public void init()
    {
        refreshCaches();
    }

    private void checkPersistent(String path)
    {
        if (!configurationPersistenceManager.isPersistent(path))
        {
            throw new IllegalArgumentException("Attempt to manage records for non-persistent path '" + path + "'");
        }
    }

    public String getOwner(String path)
    {
        String[] elements = PathUtils.getPathElements(path);
        if (elements.length > 1 && configurationPersistenceManager.getScopeInfo(elements[0]).isTemplated())
        {
            return elements[1];
        }

        return null;
    }

    public Record getRecord(String path)
    {
        checkPersistent(path);
        return templatiseRecord(path, recordManager.load(path));
    }

    /**
     * Returns the parent handle for the given record in the template
     * hierarchy or 0 if it does not exist or is not valid.
     *
     * @param path   path of the record
     * @param record record to get parent handle for
     * @return parent handle, or 0 if there is no valid parent
     */
    private long getTemplateParentHandle(String path, Record record)
    {
        String parentString = record.getMeta(PARENT_KEY);
        if (parentString != null)
        {
            try
            {
                return Long.parseLong(parentString);
            }
            catch (NumberFormatException e)
            {
                LOG.severe("Record at path '" + path + "' has illegal parent handle value '" + parentString + "'");
            }
        }

        return 0;
    }

    /**
     * Returns the path of the parent record in the template hierarchy, or
     * null if no such parent exists.
     *
     * @param path   path of the record
     * @param record record to get the parent of
     * @return the parent records path or null is there is no valid parent
     */
    private String getTemplateParentPath(String path, Record record)
    {
        String result = null;
        long handle = getTemplateParentHandle(path, record);
        if (handle != 0)
        {
            result = recordManager.getPathForHandle(handle);
            if (result == null)
            {
                LOG.severe("Record at path '" + path + "' has reference to unknown parent '" + handle + "'");
            }
        }

        return result;
    }

    private Record templatiseRecord(String path, Record record)
    {
        // We need to understand the root level can be templated.
        String[] pathElements = PathUtils.getPathElements(path);
        if (pathElements.length > 1)
        {
            ConfigurationScopeInfo scopeInfo = configurationPersistenceManager.getScopeInfo(pathElements[0]);
            if (scopeInfo.isTemplated())
            {
                // Get the top-level template record for our parent and then
                // ask it for the property to get our parent template record.
                String owningPath = PathUtils.getPath(pathElements[0], pathElements[1]);
                Record owningRecord = recordManager.load(owningPath);
                if (owningRecord == null)
                {
                    throw new IllegalArgumentException("Invalid path '" + path + "': owning record '" + owningPath + "' does not exist");
                }

                TemplateRecord parentTemplateRecord = null;
                String parentOwningPath = getTemplateParentPath(owningPath, owningRecord);
                if (parentOwningPath != null)
                {
                    parentTemplateRecord = (TemplateRecord) getRecord(parentOwningPath);
                }

                for (int i = 2; i < pathElements.length && parentTemplateRecord != null; i++)
                {
                    Object value = parentTemplateRecord.get(pathElements[i]);
                    if (value != null && !(value instanceof TemplateRecord))
                    {
                        LOG.severe("Templatising record at path '" + path + "', traverse of element '" + pathElements[1] + "' gave property of unexpected type '" + value.getClass() + "'");
                        parentTemplateRecord = null;
                        break;
                    }
                    parentTemplateRecord = (TemplateRecord) value;
                }

                if (record == null)
                {
                    record = parentTemplateRecord;
                }
                else
                {
                    ComplexType type = parentTemplateRecord == null ? configurationPersistenceManager.getType(path) : parentTemplateRecord.getType();
                    record = new TemplateRecord(pathElements[1], parentTemplateRecord, type, record);
                }
            }
        }

        return record;
    }

    public String insert(String path, Object instance)
    {
        CompositeType type = typeRegistry.getType(instance.getClass());
        if (type == null)
        {
            throw new IllegalArgumentException("Attempt to insert object of unregistered class '" + instance.getClass().getName() + "'");
        }

        try
        {
            MutableRecord record = type.unstantiate(instance);
            return insertRecord(path, record);
        }
        catch (TypeException e)
        {
            throw new ConfigRuntimeException(e);
        }
    }

    public String insertRecord(final String path, MutableRecord record)
    {
        checkPersistent(path);

        ComplexType type = getType(path);
        Record parentRecord = recordManager.load(path);

        // determine the path at which the record will be inserted.  If we are inserting into a collection,
        // this will be determined by the collection.  If we insert into a complex type, then we insert directly using
        // the path that is provided.

        String newPath = path;
        if (type instanceof CollectionType)
        {
            CollectionType collectionType = (CollectionType) type;
            if (parentRecord == null)
            {
                // Need to create the collection record.
                // FIXME: for templating, we may also want to init an order here??
                parentRecord = collectionType.createNewRecord(true);
                recordManager.insert(path, parentRecord);
            }

            String insertionPath = collectionType.getInsertionPath(parentRecord, record);
            newPath = PathUtils.getPath(path, insertionPath);
        }

        // Do some level of type checking to ensure that all is as expected when we insert into the record manager. 
        if (type instanceof CollectionType)
        {
            // If we are inserting into a collection, then the record must represent data for the collection type.
            checkRecordType((CompositeType) type.getTargetType(), record);
        }
        else
        {
            // If we are inserting into an object, then the object is defined by the parent path, and the record
            // must represent data for that objects specified property.
            String parentPath = PathUtils.getParentPath(path);
            CompositeType parentType = (CompositeType) configurationPersistenceManager.getType(parentPath);
            TypeProperty property = parentType.getProperty(PathUtils.getBaseName(path));
            checkRecordType((CompositeType) property.getType(), record);
        }

        // generate and send out events for each of the records being inserted.  The root record may itself contain
        // subrecords.

        Map<String, Record> insertedPaths = getPathRecordMapping(newPath, record);
        for (String p : insertedPaths.keySet())
        {
            Record r = insertedPaths.get(p);
            eventManager.publish(new PreInsertEvent(this, p, (MutableRecord) r));
        }

        recordManager.insert(newPath, record);
        refreshCaches();

        for (String p : insertedPaths.keySet())
        {
            eventManager.publish(new PostInsertEvent(this, p, p, instances.get(p)));
        }

        return newPath;
    }

    private void checkRecordType(CompositeType expectedType, MutableRecord record)
    {
        List<String> allowedTypes = new LinkedList<String>();
        allowedTypes.add(expectedType.getSymbolicName());
        allowedTypes.addAll(expectedType.getExtensions());
        if (!allowedTypes.contains(record.getSymbolicName()))
        {
            // need to support type extensions here.
            Type recordType = typeRegistry.getType(record.getSymbolicName());
            throw new IllegalArgumentException("Expected type: " + expectedType.getClazz() + " but instead found " + recordType.getClazz());
        }
    }

    private void refreshCaches()
    {
        configurationReferenceManager.clear();
        refreshInstances();
        refreshTemplateHierarchies();
    }

    private void refreshInstances()
    {
        instances.clear();

        for (ConfigurationScopeInfo scope : configurationPersistenceManager.getScopes())
        {
            String path = scope.getScopeName();
            Type type = scope.getType();
            Record topRecord = recordManager.load(path);

            try
            {
                if (scope.isTemplated())
                {
                    // Create the collection ourselves, and populate it with
                    // instances created from template records.
                    CollectionType collectionType = (CollectionType) type;
                    CompositeType templatedType = (CompositeType) collectionType.getCollectionType();
                    Map<String, Object> topInstance = new LinkedHashMap<String, Object>();
                    instances.put(path, topInstance);
                    for (String id : collectionType.getOrder(topRecord))
                    {
                        String itemPath = PathUtils.getPath(path, id);
                        Record record = getRecord(itemPath);
                        Object instance = templatedType.instantiate(itemPath, record);

                        // Only concrete instances go into the collection
                        if (isConcrete(record))
                        {
                            topInstance.put(id, instance);
                        }
                    }
                }
                else
                {
                    type.instantiate(path, topRecord);
                }
            }
            catch (TypeException e)
            {
                // FIXME how should we present this? i think we need to
                // FIXME store and allow the UI to show such problems
                LOG.severe("Unable to instantiate '" + path + "': " + e.getMessage(), e);
            }
        }
    }

    private boolean isConcrete(Record record)
    {
        if (record instanceof TemplateRecord)
        {
            record = ((TemplateRecord) record).getMoi();
        }
        return !Boolean.valueOf(record.getMeta(TEMPLATE_KEY));
    }

    private void refreshTemplateHierarchies()
    {
        templateHierarchies.clear();
        for (ConfigurationScopeInfo scope : configurationPersistenceManager.getScopes())
        {
            if (scope.isTemplated())
            {
                MapType type = (MapType) scope.getType();
                String idProperty = type.getKeyProperty();
                Map<String, Record> recordsByPath = new HashMap<String, Record>();
                recordManager.loadAll(PathUtils.getPath(scope.getScopeName(), PathUtils.WILDCARD_ANY_ELEMENT), recordsByPath);

                Map<Long, List<Record>> recordsByParent = new HashMap<Long, List<Record>>();
                for (Map.Entry<String, Record> entry : recordsByPath.entrySet())
                {
                    long parentHandle = getTemplateParentHandle(entry.getKey(), entry.getValue());
                    List<Record> records = recordsByParent.get(parentHandle);
                    if (records == null)
                    {
                        records = new LinkedList<Record>();
                        recordsByParent.put(parentHandle, records);
                    }

                    records.add(entry.getValue());
                }

                TemplateNode root = null;
                List<Record> rootRecords = recordsByParent.get(0L);
                if (rootRecords != null)
                {
                    if (rootRecords.size() != 1)
                    {
                        LOG.severe("Found multiple root records for scope '" + scope.getScopeName() + "': choosing an arbitrary one");
                    }

                    Record record = rootRecords.get(0);
                    root = createTemplateNode(record, scope.getScopeName(), idProperty, recordsByParent);
                }

                templateHierarchies.put(scope.getScopeName(), new TemplateHierarchy(scope.getScopeName(), root));
            }
        }
    }

    private TemplateNode createTemplateNode(Record record, String scopeName, String idProperty, Map<Long, List<Record>> recordsByParent)
    {
        String id = (String) record.get(idProperty);
        String path = PathUtils.getPath(scopeName, id);
        TemplateNode node = new TemplateNode(path, id);

        List<Record> children = recordsByParent.get(record.getHandle());
        if (children != null)
        {
            for (Record child : children)
            {
                node.addChild(createTemplateNode(child, scopeName, idProperty, recordsByParent));
            }
        }

        return node;
    }

    public Object validate(String parentPath, String baseName, Record subject, ValidationAware validationCallback)
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
            return null;
        }
        catch (TypeException e)
        {
            context.addActionError(e.getMessage());
            return null;
        }

        // Process the instance via the validation manager.
        try
        {
            validationManager.validate(instance, context);
            if (context.hasErrors())
            {
                return null;
            }
            else
            {
                return instance;
            }
        }
        catch (ValidationException e)
        {
            context.addActionError(e.getMessage());
            return null;
        }
    }

    public void save(String path, Object instance)
    {
        CompositeType type = typeRegistry.getType(instance.getClass());
        if (type == null)
        {
            throw new IllegalArgumentException("Attempt to save instance of an unknown class '" + instance.getClass().getName() + "'");
        }

        MutableRecord record;
        try
        {
            record = type.unstantiate(instance);
        }
        catch (TypeException e)
        {
            throw new ConfigRuntimeException(e);
        }
        saveRecord(path, record);
    }

    public String saveRecord(String path, MutableRecord record)
    {
        checkPersistent(path);

        Record existingRecord = getRecord(path);
        if (existingRecord == null)
        {
            throw new IllegalArgumentException("Illegal path '" + path + "': no existing record found");
        }

        // See if this is a templated scope: in which case more magic needs
        // to happen.
        if (existingRecord instanceof TemplateRecord)
        {
            // Ensure there is a concrete record for this path.  We may need
            // to create a few records along the path to make this happen.
            TemplateRecord templateRecord = (TemplateRecord) existingRecord;
            String[] pathElements = PathUtils.getPathElements(path);
            if (!templateRecord.getOwner().equals(pathElements[1]))
            {
                String owningPath = PathUtils.getPath(pathElements[0], pathElements[1]);
                Record owningRecord = recordManager.load(owningPath);

                String parentOwningPath = getTemplateParentPath(owningPath, owningRecord);
                TemplateRecord parentTemplateRecord = (TemplateRecord) getRecord(parentOwningPath);

                // Find where in the path there cease to be records in our
                // owner.  From the point where it is found, create empty
                // records to populate the remainder of the path, using the
                // type that we have inherited.
                for (int i = 2; i < pathElements.length; i++)
                {
                    String element = pathElements[i];
                    if (owningRecord != null)
                    {
                        owningRecord = (Record) owningRecord.get(element);
                    }
                    owningPath = PathUtils.getPath(owningPath, element);
                    parentTemplateRecord = (TemplateRecord) parentTemplateRecord.get(element);

                    if (owningRecord == null)
                    {
                        ComplexType type = parentTemplateRecord.getType();
                        recordManager.insert(owningPath, type.createNewRecord(false));
                    }
                }
            }

            // Scrub values from the incoming record where they are identical
            // to the existing record.
            // Perhaps we could use an iterator to remove, but does Record
            // specify what happens when removing using a key set iterator?
            Set<String> dead = new HashSet<String>();
            for(String key: record.keySet())
            {
                if(record.get(key).equals(existingRecord.get(key)))
                {
                    dead.add(key);
                }
            }

            for(String key: dead)
            {
                record.remove(key);
            }
        }

        String parentPath = PathUtils.getParentPath(path);
        String baseName = PathUtils.getBaseName(path);

        checkPersistent(parentPath);

        Record parentRecord = getRecord(parentPath);
        if (parentRecord == null)
        {
            throw new IllegalArgumentException("Invalid parent path '" + parentPath + "'");
        }

        ComplexType parentType = configurationPersistenceManager.getType(parentPath);
        Object oldInstance = instances.get(path);

        eventManager.publish(new PreSaveEvent(this, path, oldInstance));

        String newPath = path;
        if (parentType instanceof CollectionType)
        {
            CollectionType collectionType = (CollectionType) parentType;
            String newName = collectionType.getSavePath(recordManager.load(parentPath), record);
            if (baseName != null && !baseName.equals(newName))
            {
                // We need to update our own record
                newPath = PathUtils.getPath(parentPath, newName);

                recordManager.move(path, newPath);
                recordManager.update(newPath, record);
            }
            else
            {
                recordManager.update(newPath, record);
            }
        }
        else
        {
            // Regular update.
            recordManager.update(newPath, record);
        }

        refreshCaches();

        eventManager.publish(new PostSaveEvent(this, path, oldInstance, newPath, instances.get(newPath)));

        return newPath;
    }

    public void delete(final String path)
    {
        checkPersistent(path);

        // need to send out events for the individual record deletes.  How do we pick up the individual deletes.

        List<String> pathsToBeDeleted = getPathListing(path, recordManager.load(path));
        Map<String, Object> instancesToBeDeleted = getPathInstanceMapping(pathsToBeDeleted);

        for (String p : instancesToBeDeleted.keySet())
        {
            eventManager.publish(new PreDeleteEvent(this, p, instancesToBeDeleted.get(p)));
        }

        configurationReferenceManager.getCleanupTasks(path).execute();
        refreshCaches();

        for (String p : instancesToBeDeleted.keySet())
        {
            eventManager.publish(new PostDeleteEvent(this, p, instancesToBeDeleted.get(p)));
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

    @SuppressWarnings({ "unchecked" })
    public <T> T getInstance(String path, Class<T> clazz)
    {
        Object instance = getInstance(path);
        if (instance == null)
        {
            return null;
        }

        if (!clazz.isAssignableFrom(instance.getClass()))
        {
            throw new IllegalArgumentException("Path '" + path + "' does not reference an instance of type '" + clazz.getName() + "'");
        }

        return (T) instance;
    }

    public <T> Collection<T> getAllInstances(String path, Class<T> clazz)
    {
        List<T> result = new LinkedList<T>();
        getAllInstances(path, result);
        return result;
    }

    public <T> void getAllInstances(String path, Collection<T> result)
    {
        instances.getAll(path, result);
    }

    @SuppressWarnings({ "unchecked" })
    public <T> Collection<T> getAllInstances(Class<T> clazz)
    {
        CompositeType type = typeRegistry.getType(clazz);
        if (type == null)
        {
            return Collections.EMPTY_LIST;
        }

        List<T> result = new LinkedList<T>();
        List<String> paths = configurationPersistenceManager.getConfigurationPaths(type);
        if (paths != null)
        {
            for (String path : paths)
            {
                instances.getAll(path, result);
            }
        }

        return result;
    }

    public void putInstance(String path, Object instance)
    {
        instances.put(path, instance);
    }

    @SuppressWarnings({ "unchecked" })
    public <T> T getAncestorOfType(Configuration c, Class<T> clazz)
    {
        String path = c.getConfigurationPath();
        CompositeType type = typeRegistry.getType(clazz);
        if (type != null)
        {
            while (path != null)
            {
                Type pathType = configurationPersistenceManager.getType(path);
                if (pathType.equals(type))
                {
                    return (T) getInstance(path);
                }

                path = PathUtils.getParentPath(path);
            }
        }

        return null;
    }

    public void markAsTemplate(MutableRecord record)
    {
        record.putMeta(TEMPLATE_KEY, "true");
    }

    public void setParentTemplate(MutableRecord record, long parentHandle)
    {
        record.putMeta(PARENT_KEY, Long.toString(parentHandle));
    }

    public Set<String> getTemplateScopes()
    {
        return templateHierarchies.keySet();
    }

    public TemplateHierarchy getTemplateHierarchy(String scope)
    {
        ConfigurationScopeInfo scopeInfo = configurationPersistenceManager.getScopeInfo(scope);
        if (scopeInfo == null)
        {
            throw new IllegalArgumentException("Request for template hierarchy for non-existant scope '" + scope + "'");
        }

        if (!scopeInfo.isTemplated())
        {
            throw new IllegalArgumentException("Request for template hierarchy for non-templated scope '" + scope + "'");
        }

        return templateHierarchies.get(scope);
    }

    public String getTemplatePath(String path)
    {
        String templatePath = null;
        String[] elements = PathUtils.getPathElements(path);
        if (elements.length == 2)
        {
            ConfigurationScopeInfo scope = configurationPersistenceManager.getScopeInfo(elements[0]);
            if (scope != null && scope.isTemplated())
            {
                TemplateHierarchy hierarchy = templateHierarchies.get(scope.getScopeName());
                TemplateNode node = hierarchy.getNodeById(elements[1]);
                if (node != null)
                {
                    templatePath = node.getTemplatePath();
                }
            }
        }

        return templatePath;
    }

    private List<String> getPathListing(String basePath, Record record)
    {
        List<String> paths = new LinkedList<String>();
        paths.add(basePath);

        for (String key : record.keySet())
        {
            Object value = record.get(key);
            if (value instanceof Record)
            {
                String childPath = PathUtils.getPath(basePath, key);
                Record child = (Record) value;
                paths.addAll(getPathListing(childPath, child));
            }
        }
        return paths;
    }

    private Map<String, Object> getPathInstanceMapping(List<String> paths)
    {
        Map<String, Object> mapping = new HashMap<String, Object>();
        for (String path : paths)
        {
            mapping.put(path, instances.get(path));
        }
        return mapping;
    }

    private Map<String, Record> getPathRecordMapping(String basePath, Record record)
    {
        Map<String, Record> paths = new HashMap<String, Record>();
        paths.put(basePath, record);

        for (String key : record.keySet())
        {
            Object value = record.get(key);
            if (value instanceof Record)
            {
                String childPath = PathUtils.getPath(basePath, key);
                Record child = (Record) value;
                paths.putAll(getPathRecordMapping(childPath, child));
            }
        }
        return paths;
    }

    @SuppressWarnings({ "unchecked" })
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

    public ComplexType getType(String path)
    {
        String[] pathElements = PathUtils.getPathElements(path);

        // Templated paths below the owner level need special treatment.
        if (pathElements.length > 2)
        {
            ConfigurationScopeInfo scope = configurationPersistenceManager.getScopeInfo(pathElements[0]);
            if (scope == null)
            {
                throw new IllegalArgumentException("Invalid path '" + path + "':refers to unknown root scope '" + pathElements[0]);
            }

            if (scope.isTemplated())
            {
                TemplateRecord parentRecord = (TemplateRecord) getRecord(PathUtils.getParentPath(path));
                if (parentRecord == null)
                {
                    throw new IllegalArgumentException("Invalid path '" + path + "': parent path does not exist");
                }

                ComplexType parentType = parentRecord.getType();
                String baseName = pathElements[pathElements.length - 1];
                Object value = parentRecord.get(baseName);
                if (value != null)
                {
                    if (value instanceof TemplateRecord)
                    {
                        return ((TemplateRecord) value).getType();
                    }
                    else
                    {
                        throw new IllegalArgumentException("Invalid path '" + path + "': references non-complex type");
                    }
                }
                else if (parentType instanceof CompositeType)
                {
                    Type propertyType = ((CompositeType) parentType).getProperty(baseName).getType();
                    if(!(propertyType instanceof ComplexType))
                    {
                        throw new IllegalArgumentException("Invalid path '" + path + "': references non-complex type");
                    }

                    return (ComplexType) propertyType;
                }
                else
                {
                    throw new IllegalArgumentException("Invalid path '" + path + "': references unknown child '" + baseName + "' of collection");
                }
            }
        }

        return configurationPersistenceManager.getType(path);
    }

    public List<String> getRootListing()
    {
        List<String> result = new LinkedList<String>();
        for (ConfigurationScopeInfo scope : configurationPersistenceManager.getScopes())
        {
            if (scope.isPersistent())
            {
                result.add(scope.getScopeName());
            }
        }

        return result;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }

    public void setConfigurationReferenceManager(ConfigurationReferenceManager configurationReferenceManager)
    {
        this.configurationReferenceManager = configurationReferenceManager;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setValidationManager(ValidationManager validationManager)
    {
        this.validationManager = validationManager;
    }
}
