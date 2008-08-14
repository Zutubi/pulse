package com.zutubi.tove.config;

import com.zutubi.config.annotations.Wire;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.pulse.core.config.ConfigurationList;
import com.zutubi.pulse.core.config.ConfigurationMap;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.tove.config.cleanup.*;
import com.zutubi.tove.config.events.*;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.transaction.*;
import com.zutubi.tove.type.*;
import com.zutubi.tove.type.record.*;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Sort;
import com.zutubi.util.StringUtils;
import com.zutubi.util.logging.Logger;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.ValidationManager;
import com.zutubi.validation.i18n.MessagesTextProvider;
import com.zutubi.validation.i18n.TextProvider;

import java.util.*;

/**
 */
public class ConfigurationTemplateManager
{
    private static final Logger LOG = Logger.getLogger(ConfigurationTemplateManager.class);

    private StateTransactionalWrapper stateWrapper;

    private TypeRegistry typeRegistry;
    private RecordManager recordManager;
    private ConfigurationPersistenceManager configurationPersistenceManager;
    private ConfigurationReferenceManager configurationReferenceManager;
    private ConfigurationSecurityManager configurationSecurityManager;
    private ConfigurationCleanupManager configurationCleanupManager;
    private ConfigurationStateManager configurationStateManager;

    private EventManager eventManager;
    private TransactionManager transactionManager;

    private UserTransaction userTransaction;

    private ValidationManager validationManager;
    private int refreshCount = 0;
    private boolean validationEnabled = false;

    public void init()
    {
        stateWrapper = new StateTransactionalWrapper();
        stateWrapper.setTransactionManager(transactionManager);

        userTransaction = new UserTransaction(transactionManager);
    }

    public void initSecondPhase()
    {
        validationEnabled = true;
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

    /**
     * Retrieves the template parent record for the record at the given path,
     * if such a parent exists.  The given path must point to an existing
     * record.
     *
     * @param path path to retrieve the template parent record for
     * @return the template parent, or null if no such parent exists
     *         (including the case where the path is not in a templated
     *         scope)
     * @throws IllegalArgumentException if the given path does not point to
     *         an existing record
     */
    public TemplateRecord getTemplateParentRecord(String path)
    {
        checkPersistent(path);

        String[] pathElements = PathUtils.getPathElements(path);
        if (pathElements.length > 1)
        {
            ConfigurationScopeInfo scopeInfo = configurationPersistenceManager.getScopeInfo(pathElements[0]);
            if (scopeInfo.isTemplated())
            {
                return getTemplateParentRecord(pathElements);
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
    long getTemplateParentHandle(String path, Record record)
    {
        String parentString = record.getMeta(TemplateRecord.PARENT_KEY);
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
     * null if no such parent exists.  Must only be used with owning records,
     * i.e. the actual elements of the templated collection, as these are the
     * records marked with a parent handle.
     *
     * @param path     path of the given record (should be two elements)
     * @param record   record to get the parent of
     * @param required if true and the parent does not exist, an
     *                 IllegalArgumentException is thrown
     * @return the parent records path or null is there is no valid parent
     * @throws IllegalArgumentException if a required parent is not found
     */
    private String getTemplateParentPath(String path, Record record, boolean required)
    {
        if(PathUtils.getPathElements(path).length != 2)
        {
            throw new IllegalArgumentException("Path '" + path + "' does not refer to an owning record");
        }

        String result = null;
        long handle = getTemplateParentHandle(path, record);
        if (handle != 0)
        {
            result = recordManager.getPathForHandle(handle);
            if (result == null)
            {
                if(required)
                {
                    throw new IllegalArgumentException("Invalid parent handle '" + handle + "'");
                }
                else
                {
                    LOG.severe("Record at path '" + path + "' has reference to unknown parent '" + handle + "'");
                }
            }
        }

        return result;
    }

    private TemplateRecord getTemplateParentRecord(String[] pathElements)
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
        String parentOwningPath = getTemplateParentPath(owningPath, owningRecord, false);
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
                TemplateRecord parentTemplateRecord = getTemplateParentRecord(pathElements);
                ComplexType type = parentTemplateRecord == null ? configurationPersistenceManager.getType(path) : parentTemplateRecord.getType();
                record = new TemplateRecord(pathElements[1], parentTemplateRecord, type, record);
            }
        }

        return record;
    }

    <T> T executeInsideTransaction(Action<T> a)
    {
        userTransaction.begin();
        try
        {
            // Add a resource that publishes events on commit.
            Transaction txn = transactionManager.getTransaction();
            txn.enlistResource(new TransactionResource()
            {
                public boolean prepare()
                {
                    return true;
                }

                public void commit()
                {
                    ConfigurationTemplateManager.State state = getState();
                    for(ConfigurationEvent e: state.pendingEvents)
                    {
                        eventManager.publish(e);
                    }

                    state.pendingEvents.clear();
                }

                public void rollback()
                {
                    // Just don't raise the events.
                }
            });

            T result = a.execute();
            userTransaction.commit();
            return result;
        }
        catch (RuntimeException e)
        {
            userTransaction.rollback();
            throw e;
        }
        catch (Throwable t)
        {
            userTransaction.rollback();
            throw new ToveRuntimeException(t);
        }
    }

    private void publishEvent(ConfigurationEvent event)
    {
        eventManager.publish(event);
        if (event.hasExceptions())
        {
            throw new ConfigurationException(event.getExceptions().get(0));
        }
    }

    public String insert(final String path, Object instance)
    {
        CompositeType type = typeRegistry.getType(instance.getClass());
        if (type == null)
        {
            throw new IllegalArgumentException("Attempt to insert object of unregistered class '" + instance.getClass().getName() + "'");
        }

        try
        {
            final MutableRecord record = type.unstantiate(instance);
            return executeInsideTransaction(new Action<String>()
            {
                public String execute() throws Exception
                {
                    return insertRecord(path, record);
                }
            });
        }
        catch (TypeException e)
        {
            throw new ToveRuntimeException(e);
        }
    }

    public String insertRecord(final String path, final MutableRecord r)
    {
        checkPersistent(path);
        configurationSecurityManager.ensurePermission(path, AccessManager.ACTION_CREATE);

        return executeInsideTransaction(new Action<String>()
        {
            public String execute() throws Exception
            {
                MutableRecord record = r;

                ComplexType type = getType(path);

                String newPath = path;
                Type expectedType;
                if (type instanceof CollectionType)
                {
                    newPath = PathUtils.getPath(newPath, ((CollectionType)type).getItemKey(null, record));
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

                if (pathExists(newPath))
                {
                    throw new IllegalArgumentException("Invalid insertion path '" + newPath + "': record already exists (use save to modify)");
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
                        checkBasenameUniqueInHierarchy(path, newPath, elements);

                        TemplateHierarchy hierarchy = getTemplateHierarchy(scope);
                        TemplateNode node = hierarchy.getNodeById(elements[1]);

                        if (!node.isConcrete())
                        {
                            addInheritedSkeletons(elements[0], PathUtils.getPath(2, elements), actualType, record, node);
                        }
                    }
                }

                recordManager.insert(newPath, record);
                refreshCaches();
                State state = getState();
                raiseInsertEvents(state.instances, getDescendentPaths(newPath, false, true, false));

                return newPath;
            }
        });
    }

    private void checkBasenameUniqueInHierarchy(String path, String newPath, String[] newPathElements)
    {
        String ancestorPath = findAncestorPath(newPath);
        if(ancestorPath != null)
        {
            throw new IllegalArgumentException("Unable to insert record with name '" + newPathElements[newPathElements.length - 1] + "' into path '" + path + "': a record with this name already exists in ancestor '" + PathUtils.getPathElements(ancestorPath)[1] + "'");
        }

        List<String> descendentPaths = getDescendentPaths(newPath, true, false, false);
        if(descendentPaths.size() > 0)
        {
            List<String> descendentNames = CollectionUtils.map(descendentPaths, new Mapping<String, String>()
            {
                public String map(String descendentPath)
                {
                    return PathUtils.getPathElements(descendentPath)[1];
                }
            });

            throw new IllegalArgumentException("Unable to insert record with name '" + newPathElements[newPathElements.length - 1] + "' into path '" + path + "': a record with this name already exists in descendents " + descendentNames);
        }
    }

    private MutableRecord applyParentSkeleton(String newPath, MutableRecord record, CompositeType actualType)
    {
        String templateParentPath = getTemplateParentPath(newPath, record, true);
        if (templateParentPath != null)
        {
            TemplateRecord templateParent = (TemplateRecord) getRecord(templateParentPath);
            if(isConcreteOwner(templateParent))
            {
                throw new IllegalArgumentException("Cannot inherit from concrete path '" + templateParentPath + "'");
            }
            
            TemplateRecord parentSkeleton = new TemplateRecord(null, null, actualType, createSkeletonRecord(actualType, templateParent.getMoi()));
            TemplateRecord template = new TemplateRecord(null, parentSkeleton, actualType, record);
            record = template.flatten();
            scrubInheritedValues(templateParent, record, true);
        }

        return record;
    }

    private void addInheritedSkeletons(final String scope, final String remainderPath, CompositeType actualType, Record record, TemplateNode node)
    {
        final Record skeleton = createSkeletonRecord(actualType, record);

        node.forEachDescendent(new TemplateNode.NodeHandler()
        {
            public boolean handle(TemplateNode templateNode)
            {
                String descendentPath = PathUtils.getPath(scope, templateNode.getId(), remainderPath);
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

    Record createSkeletonRecord(ComplexType type, Record record)
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

    private void raiseInsertEvents(DefaultInstanceCache instances, List<String> concretePaths)
    {
        State state = getState();

        // For every new concrete path that has appeared (possibly inherited)
        for (String concretePath : concretePaths)
        {
            // Raise an event for all the config instances under this path.
            for (Object instance : instances.getAllDescendents(concretePath, false))
            {
                if (isComposite(instance))
                {
                    Configuration configuration = (Configuration) instance;
                    boolean cascaded = !concretePath.equals(configuration.getConfigurationPath());
                    InsertEvent insertEvent = new InsertEvent(this, configuration, cascaded);
                    publishEvent(insertEvent);
                    state.pendingEvents.add(new PostInsertEvent(this, configuration, cascaded));
                    configurationStateManager.createAndAssignState(configuration);
                }
            }
        }
    }

    private boolean isComposite(Object instance)
    {
        // Quicker way to get the type for existant-composites
        return instance != null && typeRegistry.getType(instance.getClass()) != null;
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
        List<CompositeType> allowedTypes = new LinkedList<CompositeType>();
        allowedTypes.add(expectedType);
        allowedTypes.addAll(expectedType.getExtensions());
        allowedTypes.addAll(expectedType.getInternalExtensions());
        CompositeType gotType = typeRegistry.getType(symbolicName);
        if (gotType == null)
        {
            throw new IllegalArgumentException("Unrecognised symbolic name '" + symbolicName + "'");
        }

        if (!allowedTypes.contains(gotType))
        {
            // need to support type extensions here.
            throw new IllegalArgumentException("Expected type: " + expectedType.getClazz() + " but instead found " + gotType.getClazz());
        }

        return gotType;
    }

    void refreshCaches()
    {
        configurationReferenceManager.clear();
        stateWrapper.execute(new TransactionalWrapper.Action<State>()
        {
            public Object execute(State state)
            {
                refreshTemplateHierarchies(state);
                refreshInstances(state);
                return null;
            }
        });
        refreshCount++;
    }

    public void wireIfRequired(Configuration instance)
    {
        CompositeType type = typeRegistry.getType(instance.getClass());
        if(type != null && type.hasAnnotation(Wire.class, true))
        {
            ComponentContext.autowire(instance);
        }
    }

    private void refreshInstances(State state)
    {
        DefaultInstanceCache instances = state.instances;
        instances.clear();

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
                instances.put(path, topInstance, true);
                for (String id : collectionType.getOrder(topRecord))
                {
                    String itemPath = PathUtils.getPath(path, id);
                    Record record = getRecord(itemPath);
                    boolean concrete = isConcreteOwner(record);
                    try
                    {
                        PersistentInstantiator instantiator = new PersistentInstantiator(path, instances, configurationReferenceManager, this);
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
                    PersistentInstantiator instantiator = new PersistentInstantiator(path, instances, configurationReferenceManager, this);
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

        if (validationEnabled)
        {
            validateInstances(instances);
        }
    }

    private void validateInstances(final InstanceCache instances)
    {
        instances.forAllInstances(new InstanceCache.InstanceHandler()
        {
            public void handle(Configuration instance, String path, boolean complete, Configuration parentInstance)
            {
                CompositeType type = typeRegistry.getType(instance.getClass());
                if (type != null)
                {
                    // Then we have a composite
                    validateInstance(type, instance, PathUtils.getParentPath(path), PathUtils.getBaseName(path), complete, true, false, null);
                    if (!instance.isValid())
                    {
                        instances.markInvalid(path);
                    }
                }
            }
        }, true);
    }

    /**
     * Tests if the given path is inherited from an ancestor template, rather
     * than being first defined at its own level of the hierarchy.  Note that
     * this method takes no account of whether the path is overridden - just
     * whether it exists in the template parent.
     *
     * If the path does not exist or is not in a templated scope, it is
     * deemed to not be inherited.
     * 
     * @param path the path to test
     * @return true if the path is inherited
     */
    public boolean existsInTemplateParent(final String path)
    {
        return executeInsideTransaction(new Action<Boolean>()
        {
            public Boolean execute() throws Exception
            {
                String parentPath = PathUtils.getParentPath(path);
                if(parentPath == null || !pathExists(parentPath))
                {
                    return false;
                }

                if(!isTemplatedPath(path))
                {
                    return false;
                }

                TemplateRecord parentTemplateParent = getTemplateParentRecord(parentPath);
                return parentTemplateParent != null && parentTemplateParent.containsKey(PathUtils.getBaseName(path));
            }
        });
    }

    /**
     * Tests if the given path is overridden in any template descendent.
     * Overrides may be indirect - for example if you have a template parent
     * and child "myscope/parent" and "myscope/child" that are identical
     * apart from an override at path "myscope/child/some/nested/value", then
     * this method will return true for "myscope/parent".
     *
     * Hiding a path in a descdent does not constitute an override.
     *
     * If the path does not exist or is not in a templated scope, this
     * method will return false.
     *
     * @param path the path to test
     * @return true if the path is overridden in any template descendent
     */
    public boolean isOverridden(final String path)
    {
        return executeInsideTransaction(new Action<Boolean>()
        {
            public Boolean execute() throws Exception
            {
                String parentPath = PathUtils.getParentPath(path);
                if(parentPath == null || !pathExists(parentPath))
                {
                    return false;
                }

                if(!isTemplatedPath(path))
                {
                    return false;
                }

                String baseName = PathUtils.getBaseName(path);
                TemplateRecord parentRecord = (TemplateRecord) getRecord(parentPath);
                String pathOwner = parentRecord.getOwner(baseName);

                List<String> descendentPaths = getDescendentPaths(parentPath, true, false, false);
                for(String descedentPath: descendentPaths)
                {
                    parentRecord = (TemplateRecord) getRecord(descedentPath);
                    if(!StringUtils.equals(pathOwner, parentRecord.getOwner(baseName)))
                    {
                        return true;
                    }
                }

                return false;
            }
        });
    }

    /**
     * Tests if an existing path is part of a concrete instance (as opposed
     * to a template).
     *
     * @see #isConcrete(String, com.zutubi.tove.type.record.Record)
     *
     * @param path the path to test, which must exist
     * @return true if the path points into a concrete instance, false if it
     *         points into a template
     * @throws IllegalArgumentException if no record exists at the path
     */
    public boolean isConcrete(String path)
    {
        if (isPersistent(path))
        {
            Record subject = getRecord(path);
            return isConcrete(PathUtils.getParentPath(path), subject);
        }
        else
        {
            return true;
        }
    }

    /**
     * Tests if the given record, inserted at the given path, would be part
     * of a concrete instance (as opposed to a template).
     *
     * @param parentPath the path the record would be inserted into
     * @param subject    the record to be inserted
     * @return true if the record given, when inserted at the path given,
     *         would be part of a concrete instance
     */
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
                    return isConcreteOwner(subject);
                }
                else
                {
                    // Load the owner to see if it is marked as a template
                    String ownerPath = PathUtils.getPath(elements[0], elements[1]);
                    Record ownerRecord = getRecord(ownerPath);
                    return isConcreteOwner(ownerRecord);
                }
            }
        }

        return true;
    }

    /**
     * Tests if a record carries the template marker, return true if it does
     * not.  Should only be applied to owner records (i.e. elements of a
     * templated collection), as the markers are only applied at this level.
     *
     * @param record the record to test
     * @return true if the record does not carry the template marker
     */
    private boolean isConcreteOwner(Record record)
    {
        if (record instanceof TemplateRecord)
        {
            record = ((TemplateRecord) record).getMoi();
        }
        return !Boolean.valueOf(record.getMeta(TemplateRecord.TEMPLATE_KEY));
    }

    private void refreshTemplateHierarchies(State state)
    {
        state.templateHierarchies.clear();
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

                state.templateHierarchies.put(scope.getScopeName(), new TemplateHierarchy(scope.getScopeName(), root));
            }
        }
    }

    private TemplateNode createTemplateNode(Record record, String scopeName, String idProperty, Map<Long, List<Record>> recordsByParent)
    {
        String id = (String) record.get(idProperty);
        String path = PathUtils.getPath(scopeName, id);
        TemplateNode node = new TemplateNode(path, id, isConcreteOwner(record));

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
        return getState().instances.isValid(path, true);
    }

    /**
     * @see #validate(String, String, com.zutubi.tove.type.record.Record, boolean, boolean, java.util.Set)
     */
    @SuppressWarnings({"JavaDoc", "unchecked"})
    public <T extends Configuration> T validate(String parentPath, String baseName, Record subject, boolean concrete, boolean deep) throws TypeException
    {
        return (T) validate(parentPath, baseName, subject, concrete, deep, null);
    }

    /**
     * Validates the given record as a composite of some type, and returns
     * the instance if valid.  The validation occurs in the context of where
     * the record is to be stored, allowing inspection of associated
     * instances if necessary.
     *
     * @param parentPath    parent of the path where the record is to be
     *                      stored
     * @param baseName      base name of the path where the record is to be
     *                      stored
     * @param subject       record to validate
     * @param concrete      if true, the record should be validated as a
     *                      concrete (i.e. complete) instance
     * @param deep          if true, child records will also be validated
     *                      recursively (otherwise they are ignored)
     * @param ignoredFields fields for which validation errors should be
     *                      ignored 
     * @return the instance, which will be marked up with any validation errors
     * @throws com.zutubi.tove.type.TypeException
     *          if an error prevents creation of the instance: this is more
     *          fatal than a normal validation problem
     */
    public <T extends Configuration> T validate(String parentPath, String baseName, Record subject, boolean concrete, boolean deep, Set<String> ignoredFields) throws TypeException
    {
        // The type we are validating against.
        CompositeType type = typeRegistry.getType(subject.getSymbolicName());
        if (type == null)
        {
            throw new TypeException("Attempt to validate record with unrecognised symbolic name '" + subject.getSymbolicName() + "'");
        }

        // Create an instance of the object represented by the record.  It is
        // during the instantiation that type conversion errors are detected.
        SimpleInstantiator instantiator = new SimpleInstantiator(configurationReferenceManager, this);
        @SuppressWarnings({"unchecked"})
        T instance = (T) instantiator.instantiate(type, subject);

        validateInstance(type, instance, parentPath, baseName, concrete, false, deep, ignoredFields);
        return instance;
    }

    /**
     * Performs a shallow validation of the given instance, recording errors
     * on the instance itself.
     *
     * @param type           instance type
     * @param instance       instance to validate
     * @param parentPath     parent path, or null if this is a top-level
     *                       instance
     * @param baseName       base name of the instances path, may be null for
     *                       a new instance
     * @param concrete       if true, the validation will check for
     *                       completeness
     * @param checkEssential if true, validation will check for essential
     *                       complex fields
     */
    public void validateInstance(CompositeType type, Configuration instance, String parentPath, String baseName, boolean concrete, boolean checkEssential)
    {
        validateInstance(type, instance, parentPath, baseName, concrete, checkEssential, false, null);
    }

    private void validateInstance(CompositeType type, Configuration instance, String parentPath, String baseName, boolean concrete, boolean checkEssential, boolean deep, Set<String> ignoredFields)
    {
        MessagesTextProvider textProvider = new MessagesTextProvider(type.getClazz());
        ValidationContext context = new ConfigurationValidationContext(instance, textProvider, parentPath, baseName, !concrete, checkEssential, this);
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
        catch (Throwable e)
        {
            LOG.severe(e);

            String message = "Unexpected error during validation: " + e.getClass().getName();
            if (e.getMessage() != null)
            {
                message += ": " + e.getMessage();
            }

            instance.addInstanceError(message);
        }

        if (deep)
        {
            validateNestedInstances(type, instance, concrete, checkEssential, ignoredFields);
        }
    }

    private void validateNestedInstances(CompositeType type, Configuration instance, boolean concrete, boolean checkEssential, Set<String> ignoredFields)
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
                    validateNestedInstance(instance, property, (CompositeType) targetType, nestedType, concrete, checkEssential);
                }
            }
        }
    }

    private void validateNestedInstance(Configuration instance, TypeProperty property, CompositeType validateType, ComplexType nestedType, boolean concrete, boolean checkEssential)
    {
        try
        {
            Configuration nestedInstance = (Configuration) property.getValue(instance);
            if (nestedInstance != null)
            {
                if (nestedType instanceof CompositeType)
                {
                    validateInstance(validateType, nestedInstance, instance.getConfigurationPath(), property.getName(), concrete, checkEssential, true, null);
                }
                else if (nestedType instanceof ListType)
                {
                    ConfigurationList list = (ConfigurationList) nestedInstance;
                    for (Object element : list)
                    {
                        validateInstance(validateType, (Configuration) element, nestedInstance.getConfigurationPath(), null, concrete, checkEssential, true, null);
                    }
                }
                else if (nestedType instanceof MapType)
                {
                    @SuppressWarnings({"unchecked"})
                    ConfigurationMap<Configuration> map = (ConfigurationMap) nestedInstance;
                    for (Map.Entry<String, Configuration> entry : map.entrySet())
                    {
                        validateInstance(validateType, entry.getValue(), nestedInstance.getConfigurationPath(), entry.getKey(), concrete, checkEssential, true, null);
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
     * {@link #saveRecord(String,com.zutubi.tove.type.record.MutableRecord,boolean)}
     * with deep set to true.  All the same restrictions apply.
     *
     * @param instance the instance to save (must already be persistent)
     * @return the path where the saved instance is stored
     * @throws IllegalArgumentException if the instance is not persistent, is
     *                                  of an unknown type or does not meet the requirements of
     *                                  saveRecord
     */
    public String save(final Configuration instance)
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

        try
        {
            final MutableRecord record = type.unstantiate(instance);

            return executeInsideTransaction(new Action<String>()
            {
                public String execute() throws Exception
                {
                    return saveRecord(instance.getConfigurationPath(), record, true);
                }
            });
        }
        catch (TypeException e)
        {
            throw new ToveRuntimeException(e);
        }

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
     * @see #saveRecord(String,com.zutubi.tove.type.record.MutableRecord,boolean)
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
    public String saveRecord(final String path, final MutableRecord record, final boolean deep)
    {
        checkPersistent(path);
        configurationSecurityManager.ensurePermission(path, AccessManager.ACTION_WRITE);

        if (record.getSymbolicName() == null)
        {
            throw new IllegalArgumentException("Record has no type (note that collections should not be saved directly)");
        }

        return executeInsideTransaction(new Action<String>()
        {
            public String execute() throws Exception
            {
                final Record existingRecord = getRecord(path);
                if (existingRecord == null)
                {
                    throw new IllegalArgumentException("Illegal path '" + path + "': no existing record found");
                }

                if(existingRecord.isCollection())
                {
                    throw new IllegalArgumentException("Illegal path '" + path + "': attempt to save a collection");
                }

                // Type check of incoming record.
                if (!existingRecord.getSymbolicName().equals(record.getSymbolicName()))
                {
                    throw new IllegalArgumentException("Saved record has type '" + record.getSymbolicName() + "' which does not match existing type '" + existingRecord.getSymbolicName() + "'");
                }

                String newPath = path;
                String parentPath = PathUtils.getParentPath(path);
                if(parentPath != null)
                {
                    ComplexType parentType = configurationPersistenceManager.getType(parentPath);
                    if(parentType instanceof CollectionType)
                    {
                        String oldKey = PathUtils.getBaseName(path);
                        CollectionType collectionType = (CollectionType) parentType;
                        String newKey = collectionType.getItemKey(path, record);
                        if(!newKey.equals(oldKey))
                        {
                            // References to the key in the parent record
                            // (e.g. in a declared order) need to be updated.
                            // Same goes for template descedents of the
                            // parent.
                            updateCollectionReferences(collectionType, parentPath, oldKey, newKey);
                        }

                        newPath = PathUtils.getPath(parentPath, newKey);
                    }
                }

                CompositeType type = typeRegistry.getType(record.getSymbolicName());

                MutableRecord newRecord = updateRecord(existingRecord, record, type);
                boolean updated = true;
                if (newPath.equals(path))
                {
                    // Regular update, first check if there are any changes
                    // to apply or if we can elide this save.
                    if(newRecord.shallowEquals(recordManager.select(path)))
                    {
                        updated = false;
                    }
                    else
                    {
                        recordManager.update(newPath, newRecord);
                    }
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

                if (updated)
                {
                    refreshCaches();
                    raiseSaveEvents(newPath);
                }

                if (deep)
                {
                    synchroniseChildRecords(newPath, existingRecord, record);
                }

                return newPath;
            }
        });
    }

    void updateCollectionReferences(CollectionType collectionType, String collectionPath, String oldKey, String newKey)
    {
        for(String path: getDescendentPaths(collectionPath, false, false, false))
        {
            MutableRecord record = recordManager.select(path).copy(false);
            if(collectionType.updateKeyReferences(record, oldKey, newKey))
            {
                recordManager.update(path, record);
            }
        }
    }

    private void raiseSaveEvents(String path)
    {
        State state = getState();

        // Pile up the events before raising any, as the handlers may make
        // their own changes!
        List<SaveEvent> saveEvents = new LinkedList<SaveEvent>();
        for (String concretePath : getDescendentPaths(path, false, true, false))
        {
            Configuration instance = state.instances.get(concretePath, false);
            if (isComposite(instance))
            {
                saveEvents.add(new SaveEvent(this, instance));
                state.pendingEvents.add(new PostSaveEvent(this, instance));
            }
        }

        for (SaveEvent e: saveEvents)
        {
            publishEvent(e);
        }
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

    void scrubInheritedValues(TemplateRecord templateParent, MutableRecord record, boolean deep)
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
                if (RecordUtils.valuesEqual(record.get(key), emptyChild.get(key)))
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

    public String findAncestorPath(String path)
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
                final String[] result = new String[1];
                TemplateHierarchy hierarchy = getTemplateHierarchy(scope);
                TemplateNode node = hierarchy.getNodeById(elements[1]);
                final String remainderPath = elements.length == 2 ? null : PathUtils.getPath(2, elements);

                node.forEachAncestor(new TemplateNode.NodeHandler()
                {
                    public boolean handle(TemplateNode node)
                    {
                        String ancestorPath = remainderPath == null ? node.getPath() : PathUtils.getPath(node.getPath(), remainderPath);
                        if (pathExists(ancestorPath))
                        {
                            result[0] = ancestorPath;
                            return false;
                        }
                        return true;
                    }
                }, true);

                return result[0];
            }
        }

        return null;
    }

    public List<String> getDescendentPaths(String path, boolean strict, final boolean concreteOnly, final boolean includeHidden)
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
                            String descendentPath = remainderPath == null ? node.getPath() : PathUtils.getPath(node.getPath(), remainderPath);
                            if(includeHidden || pathExists(descendentPath))
                            {
                                result.add(descendentPath);
                            }
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
            return Collections.emptyList();
        }
        else
        {
            return Arrays.asList(path);
        }
    }

    /**
     * Validates that a new name is unique in a collection.  This takes into
     * account names in template ancestors and descendents: the name must be
     * unique in the entire hierarchy.
     *
     * @param parentPath   path of the collection to test
     * @param baseName     the name to check for uniqueness
     * @param fieldName    the name of configuration instance field that
     *                     holds the base name (i.e. the key property)
     * @param textProvider used for formatting validation error messages
     * @throws ValidationException if the name is not unique
     */
    public void validateNameIsUnique(String parentPath, String baseName, String fieldName, TextProvider textProvider) throws ValidationException
    {
        String path = PathUtils.getPath(parentPath, baseName);
        if(pathExists(path))
        {
            throw new ValidationException(textProvider.getText(".inuse", fieldName));
        }
        else if(PathUtils.getPathElements(path).length > 2)
        {
            // We only need to do these checks when potentially
            // within a templated instance (hence the > 2 above).
            String ancestorPath = findAncestorPath(path);
            if(ancestorPath != null)
            {
                throw new ValidationException(textProvider.getText(".inancestor", fieldName, PathUtils.getPathElements(ancestorPath)[1]));
            }
            else
            {
                List<String> descendentPaths = getDescendentPaths(path, true, false, false);
                if(descendentPaths.size() > 0)
                {
                    List<String> descendentNames = CollectionUtils.map(descendentPaths, new Mapping<String, String>()
                    {
                        public String map(String descendentPath)
                        {
                            return PathUtils.getPathElements(descendentPath)[1];
                        }
                    });

                    String message;
                    if(descendentNames.size() == 1)
                    {
                        message = textProvider.getText(".indescendent", fieldName, descendentNames.get(0));
                    }
                    else
                    {
                        Collections.sort(descendentNames, new Sort.StringComparator());
                        message = textProvider.getText(".indescendents", fieldName, descendentNames.toString());
                    }

                    throw new ValidationException(message);
                }
            }
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

    /**
     * Determines if the given path exists and can be deleted.  Note that
     * this method does <b>not</b> take security considerations into account.
     *
     * @param path path to test
     * @return true if the path is deletable
     */
    public boolean canDelete(final String path)
    {
        return executeInsideTransaction(new Action<Boolean>()
        {
            public Boolean execute() throws Exception
            {
                Record record = recordManager.select(path);
                if(record == null)
                {
                    return false;
                }

                if(record.isPermanent())
                {
                    return false;
                }

                String[] pathElements = PathUtils.getPathElements(path);
                if(pathElements.length == 1)
                {
                    return false;
                }

                if(isTemplatedPath(path) && pathElements.length > 2)
                {
                    // We are deleting something inside a template: make sure
                    // it is not an inherited composite.
                    String parentPath = PathUtils.getParentPath(path);
                    String baseName = PathUtils.getBaseName(path);
                    TemplateRecord parentRecord = (TemplateRecord) getRecord(parentPath);
                    TemplateRecord parentsTemplateParent = parentRecord.getParent();

                    if(parentsTemplateParent != null && parentsTemplateParent.containsKey(baseName) && !parentRecord.isCollection())
                    {
                        return false;
                    }
                }

                return true;
            }
        });
    }


    public RecordCleanupTask getCleanupTasks(final String path)
    {
        return executeInsideTransaction(new Action<RecordCleanupTask>()
        {
            public RecordCleanupTask execute() throws Exception
            {
                Record record = recordManager.select(path);
                if(record == null)
                {
                    throw new IllegalArgumentException("Invalid path '" + path + "': does not exist");
                }

                if(record.isPermanent())
                {
                    throw new IllegalArgumentException("Cannot delete instance at path '" + path + "': marked permanent");
                }

                String[] pathElements = PathUtils.getPathElements(path);
                if(pathElements.length == 1)
                {
                    throw new IllegalArgumentException("Invalid path '" + path + "': cannot delete a scope");
                }

                if(isTemplatedPath(path))
                {
                    RecordCleanupTaskSupport result;
                    if(pathElements.length == 2)
                    {
                        // Deleting an entire templated instance
                        result = new DeleteRecordCleanupTask(path, false, recordManager);
                    }
                    else
                    {
                        // We are not deleting an entire templated instance.
                        // We need to determine if this is a hide or actual
                        // delete.
                        String parentPath = PathUtils.getParentPath(path);
                        String baseName = PathUtils.getBaseName(path);
                        TemplateRecord parentRecord = (TemplateRecord) getRecord(parentPath);
                        TemplateRecord parentsTemplateParent = parentRecord.getParent();

                        if(parentsTemplateParent == null || !parentsTemplateParent.containsKey(baseName))
                        {
                            // This record does not exist in the parent: it
                            // has been added at this level.  It should be
                            // deleted.
                            result = new DeleteRecordCleanupTask(path, false, recordManager);
                        }
                        else
                        {
                            // The record exists in the parent.  Check it is
                            // a collection item, and if so hide it.
                            if(parentRecord.isCollection())
                            {
                                result = new HideRecordCleanupTask(path, false, recordManager);
                            }
                            else
                            {
                                throw new IllegalArgumentException("Invalid path '" + path + "': cannot delete an inherited composite property");
                            }
                        }
                    }

                    addAdditionalTasks(path, result);

                    // All descendents must be deleted.
                    List<String> descendentPaths = getDescendentPaths(path, true, false, true);
                    for (String descendentPath : descendentPaths)
                    {
                        result.addCascaded(getDescendentCleanupTask(descendentPath));
                    }

                    return result;
                }
                else
                {
                    // Much simpler, just delete the record and run custom
                    // and reference cleanup tasks.
                    DeleteRecordCleanupTask result = new DeleteRecordCleanupTask(path, false, recordManager);
                    addAdditionalTasks(path, result);
                    return result;
                }
            }
        });
    }

    private RecordCleanupTask getDescendentCleanupTask(String path)
    {
        if(pathExists(path))
        {
            DeleteRecordCleanupTask result = new DeleteRecordCleanupTask(path, isSkeleton(path), recordManager);
            addAdditionalTasks(path, result);
            return result;
        }
        else
        {
            // It must be already hidden in the parent, clean up the hidden
            // key if it exists at this level.
            return new CleanupHiddenKeyCleanupTask(path, recordManager);
        }
    }

    private void addAdditionalTasks(String path, RecordCleanupTaskSupport task)
    {
        String parentPath = PathUtils.getParentPath(path);
        Record parentRecord = getRecord(parentPath);
        if(parentRecord != null && parentRecord.isCollection())
        {
            CollectionType collectionType = getType(parentPath, CollectionType.class);
            if (collectionType.isOrdered())
            {
                task.addCascaded(new CleanupOrderCleanupTask(path, recordManager));
            }
        }

        configurationCleanupManager.addCustomCleanupTasks(task);
        configurationReferenceManager.addReferenceCleanupTasks(path, task);
    }

    public void delete(final String path)
    {
        checkPersistent(path);
        configurationSecurityManager.ensurePermission(path, AccessManager.ACTION_DELETE);

        executeInsideTransaction(new Action<Object>()
        {
            public Object execute() throws Exception
            {
                Record record = recordManager.select(path);
                if(record == null)
                {
                    throw new IllegalArgumentException("No such path '" + path + "'");
                }
                if(record.isPermanent())
                {
                    throw new IllegalArgumentException("Cannot delete instance at path '" + path + "': marked permanent");
                }

                List<ConfigurationEvent> events = prepareDeleteEvents(path);
                configurationCleanupManager.runCleanupTasks(getCleanupTasks(path));
                refreshCaches();

                State state = getState();
                for(ConfigurationEvent e: events)
                {
                    if (e.isPost())
                    {
                        state.pendingEvents.add(e);
                    }
                    else
                    {
                        publishEvent(e);
                    }
                }

                return null;
            }
        });
    }

    private List<ConfigurationEvent> prepareDeleteEvents(String path)
    {
        List<ConfigurationEvent> result = new LinkedList<ConfigurationEvent>();
        State state = getState();
        for (String concretePath : getDescendentPaths(path, false, true, false))
        {
            for (Object instance : state.instances.getAllDescendents(concretePath, false))
            {
                if (isComposite(instance))
                {
                    Configuration configuration = (Configuration) instance;
                    boolean cascaded = !concretePath.equals(configuration.getConfigurationPath());
                    result.add(new DeleteEvent(this, configuration, cascaded));
                    result.add(new PostDeleteEvent(this, configuration, cascaded));
                }
            }
        }

        return result;
    }

    public int deleteAll(final String pathPattern)
    {
        return executeInsideTransaction(new Action<Integer>()
        {
            public Integer execute() throws Exception
            {
                List<String> paths = recordManager.getAllPaths(pathPattern);
                for (String path : paths)
                {
                    delete(path);
                }
                return paths.size();
            }
        });
    }

    /**
     * Restores a hidden collection item.  The path given must refer to a
     * collection item that is currently hidden.  Restoring the item causes
     * it to reappear at the restored and all descendent levels, which will
     * lead to insert events for concrete descendents.
     * 
     * @param path the path to restore: must be currently hidden
     * @throws IllegalArgumentException if path does not refer to a
     *         currently-hidden path
     */
    public void restore(final String path)
    {
        checkPersistent(path);
        configurationSecurityManager.ensurePermission(path, AccessManager.ACTION_WRITE);

        final String[] pathElements = PathUtils.getPathElements(path);
        if(pathElements.length <= 2)
        {
            throw new IllegalArgumentException("Invalid path '" + path + "': only records nested within a template can have been hidden");
        }

        final String scope = pathElements[0];
        ConfigurationScopeInfo scopeInfo = configurationPersistenceManager.getScopeInfo(scope);
        if(!scopeInfo.isTemplated())
        {
            throw new IllegalArgumentException("Invalid path '" + path + "': not a templated scope");
        }

        final String parentPath = PathUtils.getParentPath(path);

        executeInsideTransaction(new Action<Object>()
        {
            public Object execute() throws Exception
            {
                TemplateRecord parentRecord = (TemplateRecord) getRecord(parentPath);
                if(parentRecord == null)
                {
                    throw new IllegalArgumentException("Invalid path '" + path + "': parent does not exist");
                }

                MutableRecord parentCopy = parentRecord.getMoi().copy(false);
                if(!(TemplateRecord.restoreItem(parentCopy, PathUtils.getBaseName(path))))
                {
                    throw new IllegalArgumentException("Invalid path '" + path + "': not hidden");
                }

                recordManager.update(parentPath, parentCopy);

                // Now we need to restore the skeletons at this path and all
                // descendents.
                TemplateRecord templateParent = getTemplateParentRecord(pathElements);
                TemplateHierarchy templateHierarchy = getTemplateHierarchy(scope);
                TemplateNode node = templateHierarchy.getNodeById(pathElements[1]);
                addInheritedSkeletons(scope, PathUtils.getPath(2, pathElements), typeRegistry.getType(templateParent.getSymbolicName()), templateParent.getMoi(), node.getParent());

                refreshCaches();
                State state = getState();
                raiseInsertEvents(state.instances, getDescendentPaths(path, false, true, false));
                return null;
            }
        });
    }

    /**
     * Sets the order for the items in a collection.  The given path must
     * refer to an ordered collection, and all keys in the given order must
     * exist in the collection.  The given order need not contain a key for
     * every item in the collection: orders may be incomplete.
     *
     * @param path  path of an ordered collection to set the order for
     * @param order the new order of the keys in the collection
     * @throws IllegalArgumentException if path does not refer to an ordered
     *         collection or the given order contains a key that does not
     *         exist in the collection
     */
    public void setOrder(final String path, final Collection<String> order)
    {
        checkPersistent(path);
        configurationSecurityManager.ensurePermission(path, AccessManager.ACTION_WRITE);

        executeInsideTransaction(new Action<Object>()
        {
            public Object execute() throws Exception
            {
                Type type = getType(path);
                if(!(type instanceof CollectionType))
                {
                    throw new IllegalArgumentException("Invalid path '" + path + "': does not refer to a collection");
                }

                CollectionType collectionType = (CollectionType) type;
                if(!collectionType.isOrdered())
                {
                    throw new IllegalArgumentException("Invalid path '" + path + "': collection is not ordered");
                }

                Record record = getRecord(path);
                if(record == null)
                {
                    throw new IllegalArgumentException("Invalid path '" + path + "': does not exist");
                }

                for(String key: order)
                {
                    if(!record.containsKey(key))
                    {
                        throw new IllegalArgumentException("Invalid order: item '" + key + "' does not exist in collection at path '" + path + "'");
                    }
                }

                MutableRecord mutable;
                if(record instanceof TemplateRecord)
                {
                    mutable = ((TemplateRecord) record).getMoi().copy(false);
                }
                else
                {
                    mutable = record.copy(false);
                }

                CollectionType.setOrder(mutable, order);
                recordManager.update(path, mutable);

                refreshCaches();
                raiseSaveEvents(PathUtils.getParentPath(path));
                return null;
            }
        });
    }

    @SuppressWarnings({"unchecked"})
    <T extends Configuration> T deepClone(T instance)
    {
        final String path = instance.getConfigurationPath();
        Record record = getRecord(path);
        ComplexType type = getType(path);
        final DefaultInstanceCache cache = new DefaultInstanceCache();
        PersistentInstantiator instantiator = new PersistentInstantiator(path, cache, new ReferenceResolver()
        {
            public Configuration resolveReference(String fromPath, long toHandle, Instantiator instantiator) throws TypeException
            {
                InstanceSource source = getState().instances;
                String targetPath = configurationReferenceManager.getPathForHandle(toHandle);
                if(targetPath.startsWith(path))
                {
                    // This reference points within the object tree we are cloning.
                    // We must update it to point to a new clone.
                    source = new InstanceSource()
                    {
                        public Configuration get(String path, boolean allowIncomplete)
                        {
                            return cache.get(path, allowIncomplete);
                        }
                    };
                }

                return configurationReferenceManager.resolveReference(path, toHandle, instantiator, source);
            }
        }, this);

        try
        {
            return (T) instantiator.instantiate(path, false, type, record);
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
    Configuration getInstance(String path)
    {
        State state = getState();
        if (state == null)
        {
            return null;    
        }

        return state.instances.get(path, true);
    }

    ConfigurationTemplateManager.State getState()
    {
        if (stateWrapper != null)
        {
            return stateWrapper.get();
        }
        return null;
    }

    /**
     * Load the object at the specified path, ensuring that it is of the expected type.
     *
     * @param path  of the instance to retrieve
     * @param clazz defines the required type of the instance to be retrieved
     * @return instance
     */
    <T extends Configuration> T getInstance(String path, Class<T> clazz)
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

        return clazz.cast(instance);
    }

    <T extends Configuration> T getCloneOfInstance(String path, Class<T> clazz)
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
        getAllInstances(path, clazz, result, allowIncomplete);
        return result;
    }

    <T extends Configuration> void getAllInstances(String path, Class<T> clazz, Collection<T> result, boolean allowIncomplete)
    {
        State state = getState();
        state.instances.getAllMatchingPathPattern(path, clazz, result, allowIncomplete);
    }

    public <T extends Configuration> Collection<T> getAllInstances(Class<T> clazz)
    {
        CompositeType type = typeRegistry.getType(clazz);
        if (type == null)
        {
            return Collections.emptyList();
        }

        List<T> result = new LinkedList<T>();
        List<String> paths = configurationPersistenceManager.getConfigurationPaths(type);
        if (paths != null)
        {
            State state = getState();
            for (String path : paths)
            {
                state.instances.getAllMatchingPathPattern(path, clazz, result, false);
            }
        }

        return result;
    }

    <T extends Configuration> T getAncestorOfType(Configuration c, Class<T> clazz)
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
                    return clazz.cast(getInstance(path));
                }

                path = PathUtils.getParentPath(path);
            }
        }

        return null;
    }

    public void markAsTemplate(MutableRecord record)
    {
        record.putMeta(TemplateRecord.TEMPLATE_KEY, "true");
    }

    public void setParentTemplate(MutableRecord record, long parentHandle)
    {
        record.putMeta(TemplateRecord.PARENT_KEY, Long.toString(parentHandle));
    }

    public Set<String> getTemplateScopes()
    {
        return getState().templateHierarchies.keySet();
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

        return getState().templateHierarchies.get(scope);
    }

    public String getTemplatePath(String path)
    {
        TemplateNode templateNode = getTemplateNode(path);
        if (templateNode != null)
        {
            return templateNode.getTemplatePath();
        }

        return null;
    }

    /**
     * Retrieves the template node for the given path, which should be an
     * element of a templated collection.
     *
     * @param path the path to retrieve the node for
     * @return the template node, or null if this path does not refer to an
     *         element of a templated collection
     */
    public TemplateNode getTemplateNode(String path)
    {
        String[] elements = PathUtils.getPathElements(path);
        if (elements.length == 2)
        {
            ConfigurationScopeInfo scope = configurationPersistenceManager.getScopeInfo(elements[0]);
            if (scope != null && scope.isTemplated())
            {
                TemplateHierarchy hierarchy = getState().templateHierarchies.get(scope.getScopeName());
                return hierarchy.getNodeById(elements[1]);
            }
        }

        return null;
    }

    /**
     * Retrieves the type for the given path, ensuring it is of the specified
     * type.
     *
     * @see #getType(String)
     *
     * @param path      the path to retrieve the type for
     * @param typeClass class of the expected type (the result may be a
     *                  subtype)
     * @return the type of the given path
     * @throws IllegalArgumentException if the given path is not valid or
     *         does not match the expected type
     */
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

        return typeClass.cast(type);
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
                if (value != null && value instanceof TemplateRecord)
                {
                    return ((TemplateRecord) value).getType();
                }
                else if (parentType instanceof CompositeType)
                {
                    TypeProperty property = ((CompositeType) parentType).getProperty(baseName);
                    if (property != null)
                    {
                        Type propertyType = property.getType();
                        if (!(propertyType instanceof ComplexType))
                        {
                            throw new IllegalArgumentException("Invalid path '" + path + "': references non-complex type");
                        }

                        return (ComplexType) propertyType;
                    }
                    else
                    {
                        throw new IllegalArgumentException("Invalid path '" + path + "': references unknown property '" + baseName + "' of type '" + parentType.getSymbolicName() + "'");
                    }
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
            return configurationPersistenceManager.getScopeInfo(elements[0]) != null && getRecord(path) != null;
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

    /**
     * @param path the path to test
     * @return true iff the scope in which the path lies is persistent: no
     *         verification of the path occurs other than identifying the
     *         scope
     */
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

    public void setTransactionManager(TransactionManager transactionManager)
    {
        this.transactionManager = transactionManager;
    }

    public void setConfigurationCleanupManager(ConfigurationCleanupManager configurationCleanupManager)
    {
        this.configurationCleanupManager = configurationCleanupManager;
    }

    public void setConfigurationStateManager(ConfigurationStateManager configurationStateManager)
    {
        this.configurationStateManager = configurationStateManager;
    }

    interface Action<T>
    {
        T execute() throws Exception;
    }

    /**
     * The state class encapsulates the state of the configuration template manager that is subject to
     * transactional control / isolation etc.
     *
     */
    class State
    {
        /**
         * Cache of complete instances.
         */
        DefaultInstanceCache instances = new DefaultInstanceCache();
        Map<String, TemplateHierarchy> templateHierarchies = new HashMap<String, TemplateHierarchy>();

        /**
         * A list of events to be published if the current transaction commits.
         */
        List<ConfigurationEvent> pendingEvents = new LinkedList<ConfigurationEvent>();
    }

    private class StateTransactionalWrapper extends TransactionalWrapper<State>
    {
        public StateTransactionalWrapper()
        {
            super(new State());
        }

        public State copy(State v)
        {
            // caches are always fully refreshed after a change, so a standard copy is not required.
            return new State();
        }
    }
}
