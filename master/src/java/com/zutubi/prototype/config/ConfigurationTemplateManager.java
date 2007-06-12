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

    public Record getRecord(String path)
    {
        checkPersistent(path);

        Record record = recordManager.load(path);
        if (record == null)
        {
            return null;
        }
        return templatiseRecord(path, record);
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
        if(handle != 0)
        {
            result = recordManager.getPathForHandle(handle);
            if(result == null)
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

                record = new TemplateRecord(pathElements[1], parentTemplateRecord, record);
            }
        }

        return record;
    }

    public String insert(String parentPath, Object instance)
    {
        CompositeType type = typeRegistry.getType(instance.getClass());
        if (type == null)
        {
            throw new IllegalArgumentException("Attempt to insert object of unregistered class '" + instance.getClass().getName() + "'");
        }

        try
        {
            MutableRecord record = type.unstantiate(instance);
            return insertRecord(parentPath, record);
        }
        catch (TypeException e)
        {
            throw new ConfigRuntimeException(e);
        }
    }

    public String insertRecord(final String path, MutableRecord record)
    {
        checkPersistent(path);

        ComplexType type = configurationPersistenceManager.getType(path, ComplexType.class);
        eventManager.publish(new PreInsertEvent(this, path, record));

        final String result = type.insert(path, record, recordManager);
        refreshCaches();

        eventManager.publish(new PostInsertEvent(this, path, result, instances.get(result)));

        return result;
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

    private void refreshTemplateHierarchies()
    {
        templateHierarchies.clear();
        for(ConfigurationScopeInfo scope: configurationPersistenceManager.getScopes())
        {
            if(scope.isTemplated())
            {
                MapType type = (MapType) scope.getType();
                String idProperty = type.getKeyProperty();
                Map<String, Record> recordsByPath = new HashMap<String, Record>();
                recordManager.loadAll(PathUtils.getPath(scope.getScopeName(), PathUtils.WILDCARD_ANY_ELEMENT), recordsByPath);

                Map<Long, List<Record>> recordsByParent = new HashMap<Long, List<Record>>();
                for(Map.Entry<String, Record> entry: recordsByPath.entrySet())
                {
                    long parentHandle = getTemplateParentHandle(entry.getKey(), entry.getValue());
                    List<Record> records = recordsByParent.get(parentHandle);
                    if(records == null)
                    {
                        records = new LinkedList<Record>();
                        recordsByParent.put(parentHandle, records);
                    }

                    records.add(entry.getValue());
                }

                TemplateNode root = null;
                List<Record> rootRecords = recordsByParent.get(0L);
                if(rootRecords != null)
                {
                    if(rootRecords.size() != 1)
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
        TemplateNode node = new TemplateNode(null, path, id);

        List<Record> children = recordsByParent.get(record.getHandle());
        if(children != null)
        {
            for(Record child: children)
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

    public void save(String parentPath, String baseName, Object instance)
    {
        CompositeType type = typeRegistry.getType(instance.getClass());
        if (type == null)
        {
            throw new IllegalArgumentException("Attempt to save instance of an unknown class '" + instance.getClass().getName() + "'");
        }

        Record record;
        try
        {
            record = type.unstantiate(instance);
        }
        catch (TypeException e)
        {
            throw new ConfigRuntimeException(e);
        }

        saveRecord(parentPath, baseName, record);
    }


    public String saveRecord(String parentPath, String baseName, Record record)
    {
        checkPersistent(parentPath);

        Record parentRecord = recordManager.load(parentPath);
        if (parentRecord == null)
        {
            throw new IllegalArgumentException("Invalid parent path '" + parentPath + "'");
        }

        ComplexType parentType = (ComplexType) configurationPersistenceManager.getType(parentPath);

        String path = PathUtils.getPath(parentPath, baseName);
        Object oldInstance = instances.get(path);
        eventManager.publish(new PreSaveEvent(this, path, oldInstance));

        final String newPath = parentType.save(parentPath, baseName, record, recordManager);
        refreshCaches();

        eventManager.publish(new PostSaveEvent(this, path, oldInstance, newPath, instances.get(newPath)));

        return newPath;
    }

    public void delete(final String path)
    {
        checkPersistent(path);

        Object oldInstance = instances.get(path);
        eventManager.publish(new PreDeleteEvent(this, path, oldInstance));

        configurationReferenceManager.getCleanupTasks(path).execute();
        refreshCaches();

        eventManager.publish(new PostDeleteEvent(this, path, oldInstance));
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

    @SuppressWarnings({"unchecked"})
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

    @SuppressWarnings({"unchecked"})
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

    @SuppressWarnings({"unchecked"})
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

    public TemplateHierarchy getTemplateHeirarchy(String scope)
    {
        ConfigurationScopeInfo scopeInfo = configurationPersistenceManager.getScopeInfo(scope);
        if(scopeInfo == null)
        {
            throw new IllegalArgumentException("Request for template hierarchy for non-existant scope '" + scope + "'");
        }

        if(!scopeInfo.isTemplated())
        {
            throw new IllegalArgumentException("Request for template hierarchy for non-templated scope '" + scope + "'");
        }

        return templateHierarchies.get(scope);
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
