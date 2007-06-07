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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class ConfigurationTemplateManager
{
    private static final Logger LOG = Logger.getLogger(ConfigurationTemplateManager.class);

    private InstanceCache instances = new InstanceCache();

    private TypeRegistry typeRegistry;
    private RecordManager recordManager;
    private ConfigurationPersistenceManager configurationPersistenceManager;
    private ConfigurationReferenceManager configurationReferenceManager;
    private EventManager eventManager;
    private ValidationManager validationManager;

    public void init()
    {
        refreshInstances();
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

    private Record templatiseRecord(String path, Record record)
    {
        // We need to understand the root level can be templated.
        String[] pathElements = PathUtils.getPathElements(path);
        if (pathElements.length > 1)
        {
            ConfigurationScopeInfo scopeInfo = configurationPersistenceManager.getScopeInfo(pathElements[0]);
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

    public String insert(String parentPath, Object instance)
    {
        CompositeType type = typeRegistry.getType(instance.getClass());
        if (type == null)
        {
            throw new IllegalArgumentException("Attempt to insert object of unregistered class '" + instance.getClass().getName() + "'");
        }

        try
        {
            Record record = type.unstantiate(instance);
            return insertRecord(parentPath, record);
        }
        catch (TypeException e)
        {
            throw new ConfigRuntimeException(e);
        }
    }

    public String insertRecord(final String path, Record record)
    {
        checkPersistent(path);

        ComplexType type = configurationPersistenceManager.getType(path, ComplexType.class);

        eventManager.publish(new PreInsertEvent(this, path, (MutableRecord) record));

        final String result = type.insert(path, record, recordManager);
        refreshInstances();

        eventManager.publish(new PostInsertEvent(this, path, result, instances.get(result)));

        return result;
    }

    private void refreshInstances()
    {
        instances.clear();
        configurationReferenceManager.clear();

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
        refreshInstances();

        eventManager.publish(new PostSaveEvent(this, path, oldInstance, newPath, instances.get(newPath)));

        return newPath;
    }

    public void delete(final String path)
    {
        checkPersistent(path);

        Object oldInstance = instances.get(path);
        eventManager.publish(new PreDeleteEvent(this, path, oldInstance));

        configurationReferenceManager.getCleanupTasks(path).execute();
        refreshInstances();

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
