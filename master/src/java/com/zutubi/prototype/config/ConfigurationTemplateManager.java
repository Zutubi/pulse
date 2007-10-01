package com.zutubi.prototype.config;

import com.zutubi.prototype.config.events.PostDeleteEvent;
import com.zutubi.prototype.config.events.PostInsertEvent;
import com.zutubi.prototype.config.events.PostSaveEvent;
import com.zutubi.prototype.security.AccessManager;
import com.zutubi.prototype.type.*;
import com.zutubi.prototype.type.record.*;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.pulse.core.config.ConfigurationList;
import com.zutubi.pulse.core.config.ConfigurationMap;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.util.logging.Logger;
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

    private static final String PARENT_KEY    = "parentHandle";
    private static final String TEMPLATE_KEY  = "template";

    private DefaultInstanceCache instances = new DefaultInstanceCache();
    private DefaultInstanceCache incompleteInstances = new DefaultInstanceCache();
    private Map<String, TemplateHierarchy> templateHierarchies = new HashMap<String, TemplateHierarchy>();

    private TypeRegistry typeRegistry;
    private RecordManager recordManager;
    private ConfigurationPersistenceManager configurationPersistenceManager;
    private ConfigurationReferenceManager configurationReferenceManager;
    private ConfigurationSecurityManager configurationSecurityManager;
    private EventManager eventManager;
    private ValidationManager validationManager;
    private int refreshCount = 0;

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

    /**
     * Returns the record at the given path.  If the path lies within a
     * templated scope, a {@link TemplateRecord} will be returned.
     *
     * @param path path to retrieve the record for
     * @return the record at the given location, or null if no record exists
     *         at that location
     * @throws IllegalArgumentException if the path does not refer to a
     *                                  persistent scope
     */
    public Record getRecord(String path)
    {
        checkPersistent(path);
        Record record = recordManager.select(path);
        if (record != null)
        {
            record = templatiseRecord(path, record);
        }

        return record;
    }

    public TemplateRecord getParentRecord(String path)
    {
        checkPersistent(path);

        String[] pathElements = PathUtils.getPathElements(path);
        if (pathElements.length > 1)
        {
            ConfigurationScopeInfo scopeInfo = configurationPersistenceManager.getScopeInfo(pathElements[0]);
            if (scopeInfo.isTemplated())
            {
                return getParentRecord(pathElements);
            }
        }

        return null;
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

    private TemplateRecord getParentRecord(String[] pathElements)
    {
        // Get the top-level template record for our parent and then
        // ask it for the property to get our parent template record.
        String owningPath = PathUtils.getPath(pathElements[0], pathElements[1]);
        Record owningRecord = recordManager.select(owningPath);
        if (owningRecord == null)
        {
            throw new IllegalArgumentException("Invalid path '" + PathUtils.getPath(pathElements) + "': owning record '" + owningPath + "' does not exist");
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
                LOG.severe("Find parent template record for path '" + PathUtils.getPath(pathElements) + "', traverse of element '" + pathElements[1] + "' gave property of unexpected type '" + value.getClass() + "'");
                parentTemplateRecord = null;
                break;
            }

            parentTemplateRecord = (TemplateRecord) value;
        }

        return parentTemplateRecord;
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
                TemplateRecord parentTemplateRecord = getParentRecord(pathElements);
                ComplexType type = parentTemplateRecord == null ? configurationPersistenceManager.getType(path) : parentTemplateRecord.getType();
                record = new TemplateRecord(pathElements[1], parentTemplateRecord, type, record);
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

    @SuppressWarnings({"unchecked"})
    public String insertRecord(final String path, MutableRecord record)
    {
        checkPersistent(path);
        configurationSecurityManager.ensurePermission(path, AccessManager.ACTION_CREATE);

        ComplexType type = getType(path);

        // Determine the path at which the record will be inserted.  This is
        // type-dependent.
        String newPath = type.getInsertionPath(path, record);
        if (pathExists(newPath))
        {
            throw new IllegalArgumentException("Invalid insertion path '" + newPath + "': record already exists (use save to modify)");
        }

        Type expectedType;
        if (type instanceof CollectionType)
        {
            expectedType = type.getTargetType();
        }
        else
        {
            // If we are inserting into an object, then the object is defined by the parent path, and the record
            // must represent data for that objects specified property.
            String parentPath = PathUtils.getParentPath(path);
            CompositeType parentType = (CompositeType) configurationPersistenceManager.getType(parentPath);
            expectedType = parentType.getDeclaredPropertyType(PathUtils.getBaseName(path));
        }

        CompositeType actualType = checkRecordType(expectedType, record);

        // If inserting into a template path, we have two cases:
        //   - Inserting a new entry in the top collection (e.g. a project).
        //     In this case we need to build a skeleton out of the parent.
        //   - Inserting within an existing template.  In this case we need
        //     to add matching skeletons to our descendents.
        final String[] elements = PathUtils.getPathElements(newPath);
        final String scope = elements[0];
        ConfigurationScopeInfo scopeInfo = configurationPersistenceManager.getScopeInfo(scope);
        if (scopeInfo.isTemplated())
        {
            if (elements.length == 2)
            {
                // Brand new, if we have a parent we need to skeletonise.
                record = applyParentSkeleton(newPath, record, actualType);
            }
            else
            {
                TemplateHierarchy hierarchy = getTemplateHierarchy(scope);
                TemplateNode node = hierarchy.getNodeById(elements[1]);

                if (!node.isConcrete())
                {
                    addInheritedSkeletons(elements, actualType, record, node);
                }
            }
        }

        recordManager.insert(newPath, record);
        refreshCaches();
        raiseInsertEvents(getDescendentPaths(newPath, false, true));

        return newPath;
    }

    private MutableRecord applyParentSkeleton(String newPath, MutableRecord record, CompositeType actualType)
    {
        String templateParentPath = getTemplateParentPath(newPath, record);
        if (templateParentPath != null)
        {
            TemplateRecord templateParent = (TemplateRecord) getRecord(templateParentPath);
            TemplateRecord parentSkeleton = new TemplateRecord(null, null, actualType, createSkeletonRecord(actualType, templateParent.getMoi()));
            TemplateRecord template = new TemplateRecord(null, parentSkeleton, actualType, record);
            record = template.flatten();
            scrubInheritedValues(templateParent, record, true);
        }

        return record;
    }

    private void addInheritedSkeletons(final String[] pathElements, CompositeType actualType, MutableRecord record, TemplateNode node)
    {
        final String remainderPath = PathUtils.getPath(2, pathElements);
        final Record skeleton = createSkeletonRecord(actualType, record);

        node.forEachDescendent(new TemplateNode.NodeHandler()
        {
            public boolean handle(TemplateNode templateNode)
            {
                String descendentPath = PathUtils.getPath(pathElements[0], templateNode.getId(), remainderPath);
                if (recordManager.select(descendentPath) == null)
                {
                    recordManager.insert(descendentPath, skeleton);
                    return true;
                }
                else
                {
                    // We hit an existing record, bail out of this
                    // subtree.
                    return false;
                }
            }
        }, true);
    }

    private Record createSkeletonRecord(ComplexType type, Record record)
    {
        MutableRecord result = type.createNewRecord(false);
        for (String key : record.nestedKeySet())
        {
            Record child = (Record) record.get(key);
            ComplexType childType = (ComplexType) type.getActualPropertyType(key, child);
            result.put(key, createSkeletonRecord(childType, child));
        }

        return result;
    }

    private void raiseInsertEvents(List<String> concretePaths)
    {
        // For every new concrete path that has appeared (possibly inherited)
        for (String concretePath : concretePaths)
        {
            // Raise an event for all the config instances under this path.
            for (Object instance : instances.getAllDescendents(concretePath))
            {
                if (isComposite(instance))
                {
                    Configuration configuration = (Configuration) instance;
                    eventManager.publish(new PostInsertEvent(this, configuration, !concretePath.equals(configuration.getConfigurationPath())));
                    updateInternalProperties(configuration);
                }
            }
        }
    }

    private boolean isComposite(Object instance)
    {
        // Quicker way to get the type for existant-composites
        return instance != null && typeRegistry.getType(instance.getClass()) != null;
    }

    private void updateInternalProperties(Configuration configuration)
    {
        String path = configuration.getConfigurationPath();
        CompositeType type = (CompositeType) getType(path);
        if (type.hasInternalProperties())
        {
            MutableRecord mutable = recordManager.select(path).copy(false);
            for (TypeProperty property : type.getInternalProperties())
            {
                try
                {
                    Object value = property.getValue(configuration);
                    if (value != null)
                    {
                        value = property.getType().unstantiate(value);
                    }

                    if (value == null)
                    {
                        mutable.remove(property.getName());
                    }
                    else
                    {
                        mutable.put(property.getName(), value);
                    }
                }
                catch (Exception e)
                {
                    LOG.severe(e);
                }
            }

            recordManager.update(path, mutable);
        }
    }
    
    private CompositeType checkRecordType(Type expectedType, MutableRecord record)
    {
        if (!(expectedType instanceof CompositeType))
        {
            throw new IllegalArgumentException("Expected a composite type, but instead found " + expectedType.getClass().getName());
        }

        CompositeType ctype = (CompositeType) expectedType;
        String symbolicName = record.getSymbolicName();
        return typeCheck(ctype, symbolicName);
    }

    /**
     * Checks that the type referred to by the given symbolic name is
     * compatible with an expected type.  To be compatible, the symbolic name
     * must refer to the expected type or one of its extensions.
     *
     * @param expectedType the type that we expect
     * @param symbolicName symbolic name of the type we have been given
     * @return the type we have been given
     * @throws IllegalArgumentException if the symbolic name does not refer
     *                                  to a compatible type
     */
    public CompositeType typeCheck(CompositeType expectedType, String symbolicName)
    {
        List<String> allowedTypes = new LinkedList<String>();
        allowedTypes.add(expectedType.getSymbolicName());
        allowedTypes.addAll(expectedType.getExtensions());
        allowedTypes.addAll(expectedType.getInternalExtensions());
        CompositeType gotType = typeRegistry.getType(symbolicName);
        if (gotType == null)
        {
            throw new IllegalArgumentException("Unrecognised symbolic name '" + symbolicName + "'");
        }

        if (!allowedTypes.contains(symbolicName))
        {
            // need to support type extensions here.
            throw new IllegalArgumentException("Expected type: " + expectedType.getClazz() + " but instead found " + gotType.getClazz());
        }

        return gotType;
    }

    private void refreshCaches()
    {
        configurationReferenceManager.clear();
        refreshInstances();
        refreshTemplateHierarchies();
        refreshCount++;
    }

    private void refreshInstances()
    {
        instances.clear();
        incompleteInstances.clear();

        for (ConfigurationScopeInfo scope : configurationPersistenceManager.getScopes())
        {
            String path = scope.getScopeName();
            Type type = scope.getType();
            Record topRecord = recordManager.select(path);

            if (scope.isTemplated())
            {
                // Create the collection ourselves, and populate it with
                // instances created from template records.
                CollectionType collectionType = (CollectionType) type;
                CompositeType templatedType = (CompositeType) collectionType.getCollectionType();
                ConfigurationMap<Configuration> topInstance = new ConfigurationMap<Configuration>();
                instances.put(path, topInstance);
                for (String id : collectionType.getOrder(topRecord))
                {
                    String itemPath = PathUtils.getPath(path, id);
                    Record record = getRecord(itemPath);
                    boolean concrete = isConcrete(record);
                    try
                    {
                        PersistentInstantiator instantiator = new PersistentInstantiator(path, concrete, instances, incompleteInstances, configurationReferenceManager);
                        Configuration instance = (Configuration) instantiator.instantiate(id, true, templatedType, record);

                        // Concrete instances go into the collection
                        if (concrete)
                        {
                            topInstance.put(id, instance);
                        }
                    }
                    catch (TypeException e)
                    {
                        topInstance.addFieldError(id, e.getMessage());
                    }
                }
            }
            else
            {
                try
                {
                    PersistentInstantiator instantiator = new PersistentInstantiator(path, true, instances, incompleteInstances, configurationReferenceManager);
                    instantiator.instantiate(path, false, type, topRecord);
                }
                catch (TypeException e)
                {
                    // This is pretty fatal, but should only happen if
                    // there is a programming error in Pulse or severe
                    // data corruption.
                    LOG.severe("Unable to instantiate object at root of scope '" + path + "': " + e.getMessage(), e);
                }
            }
        }

        validateInstances(instances, true);
        validateInstances(incompleteInstances, false);
    }

    private void validateInstances(final InstanceCache instances, final boolean concrete)
    {
        instances.forAllInstances(new InstanceCache.InstanceHandler()
        {
            public void handle(Configuration instance, String path, Configuration parentInstance)
            {
                CompositeType type = typeRegistry.getType(instance.getClass());
                if (type != null)
                {
                    // Then we have a composite
                    validateInstance(type, instance, parentInstance, PathUtils.getBaseName(path), concrete, false, null);
                    if (!instance.isValid())
                    {
                        instances.markInvalid(path);
                    }
                }
            }
        });
    }

    public boolean isConcrete(String parentPath, Record subject)
    {
        if (parentPath != null)
        {
            String[] elements = PathUtils.getPathElements(parentPath);
            if (configurationPersistenceManager.getScopeInfo(elements[0]).isTemplated())
            {
                if (elements.length == 1)
                {
                    // This record itself will carry the template marker
                    return isConcrete(subject);
                }
                else
                {
                    // Load the owner to see if it is marked as a template
                    String ownerPath = PathUtils.getPath(elements[0], elements[1]);
                    Record ownerRecord = getRecord(ownerPath);
                    return isConcrete(ownerRecord);
                }
            }
        }

        return true;
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
                Map<String, Record> recordsByPath = recordManager.selectAll(PathUtils.getPath(scope.getScopeName(), PathUtils.WILDCARD_ANY_ELEMENT));


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
        TemplateNode node = new TemplateNode(path, id, isConcrete(record));

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

    /**
     * Indicates if an instance and all instances reachable via its
     * properties are valid.
     * For example, for a project, indicates if the entire project
     * configuration (including the SCM, triggers etc) is valid.
     *
     * @param path the path to test
     * @return true if all instances under the path are valid
     */
    public boolean isDeeplyValid(String path)
    {
        if (instances.get(path) != null)
        {
            return instances.isValid(path);
        }
        else
        {
            return incompleteInstances.isValid(path);
        }
    }

    /**
     * Validates the given record as a composite of some type, and returns
     * the instance if valid.  The validation occurs in the context of where
     * the record is to be stored, allowing inspection of associated
     * instances if necessary.
     *
     * @param parentPath parent of the path where the record is to be stored
     * @param baseName   base name of the path where the record is to be
     *                   stored
     * @param subject    record to validate
     * @param deep       if true, child records will also be validated
     *                   recursively (otherwise they are ignored)
     * @return the instance if valid, null otherwise
     * @throws com.zutubi.prototype.type.TypeException
     *          if an error prevents
     *          creation of the instance: this is motre fatal than a normal
     *          validation problem
     */
    @SuppressWarnings({"unchecked"})
    public <T> T validate(String parentPath, String baseName, Record subject, boolean deep) throws TypeException
    {
        return (T) validate(parentPath, baseName, subject, deep, null);
    }

    @SuppressWarnings({"unchecked"})
    public <T> T validate(String parentPath, String baseName, Record subject, boolean deep, Set<String> ignoredFields) throws TypeException
    {
        // The type we are validating against.
        CompositeType type = typeRegistry.getType(subject.getSymbolicName());
        if (type == null)
        {
            throw new TypeException("Attempt to validate record with unrecognised symbolic name '" + subject.getSymbolicName() + "'");
        }

        // Create an instance of the object represented by the record.  It is
        // during the instantiation that type conversion errors are detected.
        Configuration instance;
        SimpleInstantiator instantiator = new SimpleInstantiator(configurationReferenceManager);
        instance = (Configuration) instantiator.instantiate(type, subject);

        // Now apply validations via using the validation manager.
        Configuration parentInstance = parentPath == null ? null : getInstance(parentPath);
        boolean concrete = isConcrete(parentPath, subject);
        validateInstance(type, instance, parentInstance, baseName, concrete, deep, ignoredFields);

        return (T) instance;
    }

    /**
     * Performs a shallow validation of the given instance, recording errors
     * on the instance itself.
     *
     * @param type           instance type
     * @param instance       instance to validate
     * @param parentInstance parent instance, or null if this is a top-level
     *                       instance
     * @param baseName       base name of the instances path, may be null for
     *                       a new instance
     * @param concrete       if true, the validation will check for
     *                       completeness
     */
    public void validateInstance(CompositeType type, Configuration instance, Configuration parentInstance, String baseName, boolean concrete)
    {
        validateInstance(type, instance, parentInstance, baseName, concrete, false, null);
    }

    private void validateInstance(CompositeType type, Configuration instance, Configuration parentInstance, String baseName, boolean concrete, boolean deep, Set<String> ignoredFields)
    {
        MessagesTextProvider textProvider = new MessagesTextProvider(type.getClazz());
        ValidationContext context = new ConfigurationValidationContext(instance, textProvider, parentInstance, baseName, !concrete);
        if (ignoredFields != null)
        {
            context.addIgnoredFields(ignoredFields);
        }

        try
        {
            validationManager.validate(instance, context);
        }
        catch (ValidationException e)
        {
            instance.addInstanceError(e.getMessage());
        }

        if (deep)
        {
            validateNestedInstances(type, instance, concrete, ignoredFields);
        }
    }

    private void validateNestedInstances(CompositeType type, Configuration instance, boolean concrete, Set<String> ignoredFields)
    {
        for (String key : type.getPropertyNames(ComplexType.class))
        {
            if (ignoredFields == null || !ignoredFields.contains(key))
            {
                TypeProperty property = type.getProperty(key);
                ComplexType nestedType = (ComplexType) property.getType();
                Type targetType = nestedType.getTargetType();
                if (targetType instanceof CompositeType)
                {
                    validateNestedInstance(instance, property, (CompositeType) targetType, nestedType, concrete);
                }
            }
        }
    }

    private void validateNestedInstance(Configuration instance, TypeProperty property, CompositeType validateType, ComplexType nestedType, boolean concrete)
    {
        try
        {
            Configuration nestedInstance = (Configuration) property.getValue(instance);
            if (nestedInstance != null)
            {
                if (nestedType instanceof CompositeType)
                {
                    validateInstance(validateType, nestedInstance, instance, property.getName(), concrete, true, null);
                }
                else if (nestedType instanceof ListType)
                {
                    ConfigurationList list = (ConfigurationList) nestedInstance;
                    for (Object element : list)
                    {
                        validateInstance(validateType, (Configuration) element, nestedInstance, null, concrete, true, null);
                    }
                }
                else if (nestedType instanceof MapType)
                {
                    ConfigurationMap<Configuration> map = (ConfigurationMap) nestedInstance;
                    for (Map.Entry<String, Configuration> entry : map.entrySet())
                    {
                        validateInstance(validateType, entry.getValue(), nestedInstance, entry.getKey(), concrete, true, null);
                    }
                }
                else
                {
                    throw new ValidationException("Property has unrecognised type '" + nestedType.getClass().getName() + "'");
                }
            }
        }
        catch (Exception e)
        {
            LOG.severe(e);
            instance.addFieldError(property.getName(), "Unable to apply deep validation to property: " + e.getMessage());
        }
    }

    /**
     * Saves the given instance at the given path.  This is the same as doing
     * a conversion to a record and saving using
     * {@link #saveRecord(String,com.zutubi.prototype.type.record.MutableRecord,boolean)}
     * with deep set to true.  All the same restrictions apply.
     *
     * @param instance the instance to save (must already be persistent)
     * @return the path where the saved instance is stored
     * @throws IllegalArgumentException if the instance is not persistent, is
     *                                  of an unknown type or does not meet the requirements of
     *                                  saveRecord
     */
    public String save(Configuration instance)
    {
        if (instance.getConfigurationPath() == null)
        {
            throw new IllegalArgumentException("Instance does not appear to be persistent (configuration path is unset), use insert for new instances");
        }

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

        return saveRecord(instance.getConfigurationPath(), record, true);
    }

    /**
     * Performs a shallow save of the given record at the given path.
     *
     * @param path   the path to save the record to
     * @param record the record to save
     * @return the path that the saved record is stored at, which may be
     *         different from the save path
     * @throws IllegalArgumentException when the arguments do not meet the
     *                                  criteria outlined
     * @see #saveRecord(String,com.zutubi.prototype.type.record.MutableRecord,boolean)
     */
    public String saveRecord(String path, MutableRecord record)
    {
        return saveRecord(path, record, false);
    }

    /**
     * Saves the given record to the given path.  The record must be of
     * composite type, and a record of the same type must already exist at
     * the path.  To manipulate collections or change the type, delete and
     * insert operations should be used.  Child records are ignored unless
     * deep is true, in which case they are also saved (transitively).  In
     * this case deletes and inserts may occur at child paths.
     *
     * @param path   the path to save the record to
     * @param record the record to save
     * @param deep   if true, the state of child records is updated to match
     *               the incoming record using delete, insert and save
     *               operations as necessary (normally child records are
     *               ignored)
     * @return the path that the saved record is stored at, which may be
     *         different from the save path
     * @throws IllegalArgumentException when the arguments do not meet the
     *                                  criteria outlined
     */
    public String saveRecord(String path, MutableRecord record, boolean deep)
    {
        checkPersistent(path);
        configurationSecurityManager.ensurePermission(path, AccessManager.ACTION_WRITE);

        if (record.getSymbolicName() == null)
        {
            throw new IllegalArgumentException("Record has no type (note that collections should not be saved directly)");
        }

        Record existingRecord = getRecord(path);
        if (existingRecord == null)
        {
            throw new IllegalArgumentException("Illegal path '" + path + "': no existing record found");
        }

        String parentPath = PathUtils.getParentPath(path);
        if (parentPath == null)
        {
            throw new IllegalArgumentException("Illegal path '" + path + "': no parent record");
        }

        // Type check of incoming record.
        if (!existingRecord.getSymbolicName().equals(record.getSymbolicName()))
        {
            throw new IllegalArgumentException("Saved record has type '" + record.getSymbolicName() + "' which does not match existing type '" + existingRecord.getSymbolicName() + "'");
        }

        ComplexType parentType = configurationPersistenceManager.getType(parentPath);
        String newPath = parentType.getSavePath(path, record);
        CompositeType type = typeRegistry.getType(record.getSymbolicName());

        MutableRecord newRecord = updateRecord(existingRecord, record, type);
        if (newPath.equals(path))
        {
            // Regular update
            recordManager.update(newPath, newRecord);
        }
        else
        {
            // We need to update the path by moving.  If templating, this
            // means also moving all children, *except* at the top level.
            if (existingRecord instanceof TemplateRecord)
            {
                moveDescendents(path, newPath);
            }

            recordManager.move(path, newPath);
            recordManager.update(newPath, newRecord);
        }

        refreshCaches();

        for (String concretePath : getDescendentPaths(newPath, false, true))
        {
            Configuration instance = instances.get(concretePath);
            if (isComposite(instance))
            {
                eventManager.publish(new PostSaveEvent(this, instance));
            }
        }

        if (deep)
        {
            synchroniseChildRecords(newPath, existingRecord, record);
        }

        return newPath;
    }

    private void synchroniseChildRecords(String path, Record existingRecord, MutableRecord record)
    {
        Set<String> existingChildren = existingRecord.nestedKeySet();
        Set<String> newChildren = record.nestedKeySet();

        // Discover changed and inserted children
        for (String key : newChildren)
        {
            String childPath = PathUtils.getPath(path, key);
            MutableRecord child = (MutableRecord) record.get(key);

            if (existingChildren.contains(key))
            {
                if (child.isCollection())
                {
                    // Jump down a level to synchronise elements
                    synchroniseChildRecords(childPath, (Record) existingRecord.get(key), child);
                }
                else
                {
                    // Save this child composite
                    saveRecord(childPath, child, true);
                }
            }
            else
            {
                // Insert new child.
                String insertPath;
                if (record.isCollection())
                {
                    insertPath = path;
                }
                else
                {
                    insertPath = childPath;
                }

                insertRecord(insertPath, child);
            }
        }

        // Discover deleted children
        for (String key : existingChildren)
        {
            if (!newChildren.contains(key))
            {
                delete(PathUtils.getPath(path, key));
            }
        }
    }

    private MutableRecord updateRecord(Record existingRecord, MutableRecord updates, CompositeType type)
    {
        MutableRecord newRecord;
        if (existingRecord instanceof TemplateRecord)
        {
            TemplateRecord templateRecord = (TemplateRecord) existingRecord;
            newRecord = templateRecord.getMoi().copy(false);
            newRecord.update(updates);

            // Scrub values from the incoming record where they are identical
            // to the existing record's parent.
            scrubInheritedValues(templateRecord, newRecord, type);
        }
        else
        {
            newRecord = existingRecord.copy(false);
            newRecord.update(updates);
        }

        return newRecord;
    }

    private void moveDescendents(String path, String newPath)
    {
        String[] elements = PathUtils.getPathElements(path);
        if (elements.length > 2)
        {
            final String scope = elements[0];
            String newName = PathUtils.getBaseName(newPath);

            final String oldRemainderPath = PathUtils.getPath(2, elements);
            final String newRemainderPath = PathUtils.getPath(PathUtils.getParentPath(oldRemainderPath), newName);
            TemplateHierarchy hierarchy = getTemplateHierarchy(scope);
            TemplateNode node = hierarchy.getNodeById(elements[1]);

            node.forEachDescendent(new TemplateNode.NodeHandler()
            {
                public boolean handle(TemplateNode templateNode)
                {
                    String oldDescendentPath = PathUtils.getPath(scope, templateNode.getId(), oldRemainderPath);
                    String newDescendentPath = PathUtils.getPath(scope, templateNode.getId(), newRemainderPath);
                    recordManager.move(oldDescendentPath, newDescendentPath);
                    return true;
                }
            }, true);
        }
    }

    public void scrubInheritedValues(TemplateRecord templateRecord, MutableRecord record, CompositeType type)
    {
        TemplateRecord existingParent = templateRecord.getParent();
        if (existingParent != null)
        {
            scrubInheritedValues(existingParent, record, false);
        }
    }

    private void scrubInheritedValues(TemplateRecord templateParent, MutableRecord record, boolean deep)
    {
        ComplexType type = templateParent.getType();
        if (type instanceof CompositeType)
        {
            // We add an empty layer where we are about to add the new
            // record in case there are any values hidden from the parent
            // via templating rules (like NoInherit).
            TemplateRecord emptyChild = new TemplateRecord(null, templateParent, type, type.createNewRecord(false));
            Set<String> dead = new HashSet<String>();

            // Perhaps we could use an iterator to remove, but does Record
            // specify what happens when removing using a key set iterator?
            for (String key : record.simpleKeySet())
            {
                if (record.get(key).equals(emptyChild.get(key)))
                {
                    dead.add(key);
                }
            }

            for (String key : dead)
            {
                record.remove(key);
            }
        }

        if (deep)
        {
            for (String key : record.nestedKeySet())
            {
                TemplateRecord propertyParent = (TemplateRecord) templateParent.get(key);
                if (propertyParent != null)
                {
                    scrubInheritedValues(propertyParent, (MutableRecord) record.get(key), true);
                }
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    public List<String> getDescendentPaths(String path, boolean strict, final boolean concreteOnly)
    {
        String[] elements = PathUtils.getPathElements(path);
        if (elements.length > 1)
        {
            String scope = elements[0];
            ConfigurationScopeInfo scopeInfo = configurationPersistenceManager.getScopeInfo(scope);
            if (scopeInfo == null)
            {
                throw new IllegalArgumentException("Invalid path '" + path + "': references unknown scope '" + scope + "'");
            }

            if (scopeInfo.isTemplated())
            {
                final List<String> result = new LinkedList<String>();
                TemplateHierarchy hierarchy = getTemplateHierarchy(scope);
                TemplateNode node = hierarchy.getNodeById(elements[1]);
                final String remainderPath = elements.length == 2 ? null : PathUtils.getPath(2, elements);

                node.forEachDescendent(new TemplateNode.NodeHandler()
                {
                    public boolean handle(TemplateNode node)
                    {
                        if (!concreteOnly || node.isConcrete())
                        {
                            result.add(remainderPath == null ? node.getPath() : PathUtils.getPath(node.getPath(), remainderPath));
                        }
                        return true;
                    }
                }, strict);

                return result;
            }
        }

        // We get here for non-templated scopes.
        if (strict)
        {
            return Collections.EMPTY_LIST;
        }
        else
        {
            return Arrays.asList(path);
        }
    }

    private boolean isSkeleton(String path)
    {
        String[] elements = PathUtils.getPathElements(path);
        if (elements.length > 2)
        {
            String scope = elements[0];
            ConfigurationScopeInfo scopeInfo = configurationPersistenceManager.getScopeInfo(scope);
            if (scopeInfo == null)
            {
                throw new IllegalArgumentException("Invalid path '" + path + "': references unknown scope '" + scope + "'");
            }

            if (scopeInfo.isTemplated())
            {
                TemplateRecord record = (TemplateRecord) getRecord(path);
                if (record == null)
                {
                    throw new IllegalArgumentException("Invalid path '" + path + "': no record found");
                }

                return record.isSkeleton();
            }
        }

        return false;
    }

    public RecordCleanupTask getCleanupTasks(String path)
    {
        DeleteRecordCleanupTask result = new DeleteRecordCleanupTask(path, isSkeleton(path), recordManager);

        // If this is a templated scope, all descendents must also be deleted
        List<String> descendentPaths = getDescendentPaths(path, true, false);
        for (String descendentPath : descendentPaths)
        {
            result.addCascaded(getCleanupTasks(descendentPath));
        }

        configurationReferenceManager.addReferenceCleanupTasks(path, result);
        return result;
    }

    public void delete(final String path)
    {
        checkPersistent(path);
        configurationSecurityManager.ensurePermission(path, AccessManager.ACTION_DELETE);

        Record record = recordManager.select(path);
        if(record == null)
        {
            throw new IllegalArgumentException("No such path '" + path + "'");
        }
        if(record.isPermanent())
        {
            throw new IllegalArgumentException("Cannot delete instance at path '" + path + "': marked permanent");
        }
        
        List<PostDeleteEvent> events = new LinkedList<PostDeleteEvent>();
        for (String concretePath : getDescendentPaths(path, false, true))
        {
            for (Object instance : instances.getAllDescendents(concretePath))
            {
                if (isComposite(instance))
                {
                    Configuration configuration = (Configuration) instance;
                    events.add(new PostDeleteEvent(this, configuration, !concretePath.equals(configuration.getConfigurationPath())));
                }
            }
        }

        getCleanupTasks(path).execute();
        refreshCaches();

        for (PostDeleteEvent event : events)
        {
            eventManager.publish(event);
        }
    }

    public int deleteAll(final String pathPattern)
    {
        List<String> paths = recordManager.getAllPaths(pathPattern);
        for (String path : paths)
        {
            delete(path);
        }
        return paths.size();
    }

    @SuppressWarnings({"unchecked"})
    public <T extends Configuration> T deepClone(T instance)
    {
        String path = instance.getConfigurationPath();
        Record record = getRecord(path);
        ComplexType type = getType(path);
        PersistentInstantiator instantiator = new PersistentInstantiator(path, isConcrete(PathUtils.getParentPath(path), record), new DefaultInstanceCache(), new DefaultInstanceCache(), configurationReferenceManager);
        try
        {
            Configuration clone = (Configuration) instantiator.instantiate(path, false, type, record);
            return (T) clone;
        }
        catch (TypeException e)
        {
            LOG.severe(e);
            return null;
        }
    }

    /**
     * Load the object at the specified path, or null if no object exists.
     *
     * @param path path of the instance to retrieve
     * @return object defined by the path.
     */
    public Configuration getInstance(String path)
    {
        Configuration instance = instances.get(path);
        if (instance == null)
        {
            instance = incompleteInstances.get(path);
        }

        return instance;
    }

    /**
     * Load the object at the specified path, ensuring that it is of the expected type.
     *
     * @param path  of the instance to retrieve
     * @param clazz defines the required type of the instance to be retrieved
     * @return instance
     */
    @SuppressWarnings({"unchecked"})
    public <T extends Configuration> T getInstance(String path, Class<T> clazz)
    {
        Configuration instance = getInstance(path);
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

    public <T extends Configuration> T getCloneOfInstance(String path, Class<T> clazz)
    {
        T instance = getInstance(path, clazz);
        if (instance == null)
        {
            return null;
        }

        return deepClone(instance);
    }

    public <T extends Configuration> Collection<T> getAllInstances(String path, Class<T> clazz, boolean allowIncomplete)
    {
        List<T> result = new LinkedList<T>();
        getAllInstances(path, result, allowIncomplete);
        return result;
    }

    @SuppressWarnings({"unchecked"})
    public <T extends Configuration> void getAllInstances(String path, Collection<T> result, boolean allowIncomplete)
    {
        instances.getAllMatchingPathPattern(path, (Collection<Configuration>) result);
        if (allowIncomplete)
        {
            incompleteInstances.getAllMatchingPathPattern(path, (Collection<Configuration>) result);
        }
    }

    @SuppressWarnings({"unchecked"})
    public <T extends Configuration> Collection<T> getAllInstances(Class<T> clazz)
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
                instances.getAllMatchingPathPattern(path, (Collection<Configuration>) result);
            }
        }

        return result;
    }

    @SuppressWarnings({"unchecked"})
    public <T extends Configuration> T getAncestorOfType(Configuration c, Class<T> clazz)
    {
        CompositeType type = typeRegistry.getType(clazz);
        if (type != null)
        {
            String path = PathUtils.getParentPath(c.getConfigurationPath());
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

    @SuppressWarnings({"unchecked"})
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
                throw new IllegalArgumentException("Invalid path '" + path + "': refers to unknown root scope '" + pathElements[0]);
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
                    if (!(propertyType instanceof ComplexType))
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

    /**
     * @param path the path to test
     * @return true if the given path refers to an existing record
     */
    public boolean pathExists(String path)
    {
        if (path.length() == 0)
        {
            return false;
        }
        else
        {
            String[] elements = PathUtils.getPathElements(path);
            if (configurationPersistenceManager.getScopeInfo(elements[0]) == null)
            {
                return false;
            }

            return getRecord(path) != null;
        }
    }

    /**
     * Returns true iff the given path is at or under a template scope.  The
     * validity of the path is not checked.
     *
     * @param path the path test test
     * @return true iff the path is templated
     */
    public boolean isTemplatedPath(String path)
    {
        String[] elements = PathUtils.getPathElements(path);
        if (elements.length >= 1)
        {
            ConfigurationScopeInfo info = configurationPersistenceManager.getScopeInfo(elements[0]);
            if (info != null && info.isTemplated())
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if the given path points to a templated collection: i.e.
     * a collection at the root of a templated scope.
     *
     * @param path the path to test
     * @return true iff path points to a templated collection
     */
    public boolean isTemplatedCollection(String path)
    {
        String[] elements = PathUtils.getPathElements(path);
        if (elements.length == 1)
        {
            ConfigurationScopeInfo info = configurationPersistenceManager.getScopeInfo(elements[0]);
            if (info != null && info.isTemplated())
            {
                return true;
            }
        }

        return false;
    }

    public boolean isPersistent(String path)
    {
        return configurationPersistenceManager.isPersistent(path);
    }

    public int getRefreshCount()
    {
        return refreshCount;
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

    public void setConfigurationSecurityManager(ConfigurationSecurityManager configurationSecurityManager)
    {
        this.configurationSecurityManager = configurationSecurityManager;
    }
}
