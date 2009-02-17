package com.zutubi.tove.config;

import com.zutubi.tove.annotations.Reference;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.config.cleanup.DefaultReferenceCleanupTaskProvider;
import com.zutubi.tove.config.cleanup.RecordCleanupTask;
import com.zutubi.tove.config.cleanup.RecordCleanupTaskSupport;
import com.zutubi.tove.config.cleanup.ReferenceCleanupTaskProvider;
import com.zutubi.tove.type.*;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.tove.type.record.TemplateRecord;
import com.zutubi.util.ClassLoaderUtils;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import java.util.*;

/**
 * Manages the resolution, maintenance and cleanup of references between
 * configuration instances.
 */
public class ConfigurationReferenceManager implements ReferenceResolver
{
    private static final Logger LOG = Logger.getLogger(ConfigurationReferenceManager.class);

    /**
     * Mapping from a path to all other paths that reference it.  It is safe
     * to use paths here as this index is completely refreshed when the
     * instance cache is: i.e. on any change.
     */
    private Map<String, List<String>> references = new HashMap<String, List<String>>();

    private TypeRegistry typeRegistry;
    private RecordManager recordManager;
    private ConfigurationPersistenceManager configurationPersistenceManager;
    private ConfigurationTemplateManager configurationTemplateManager;
    private ObjectFactory objectFactory;

    public void clear()
    {
        references.clear();
    }

    /**
     * Returns the path that is referenced by the given handle, which may not
     * be the path for that handle directly in a templated scope.  In templated
     * scopes references always use the handle where an instance is first
     * defined in the hierarchy.
     *
     * @param templateOwnerPath if in a templated scope, the item of the
     *                          templated collection that this reference is
     *                          coming from, otherwise null
     * @param toHandle          the handle being referenced
     * @return the path referenced by the given handle, taking into account any
     *         template owner
     */
    public String getReferencedPathForHandle(String templateOwnerPath, long toHandle)
    {
        String toPath = recordManager.getPathForHandle(toHandle);
        if (templateOwnerPath != null)
        {
            // If we have a more local inherited version of toPath, push the
            // path down to the local template's level.
            String toTemplateOwnerPath = configurationTemplateManager.getTemplateOwnerPath(toPath);
            if (toTemplateOwnerPath != null)
            {
                if (isTemplateAncestor(toTemplateOwnerPath, templateOwnerPath))
                {
                    String suffix = PathUtils.getPath(2, PathUtils.getPathElements(toPath));
                    toPath = PathUtils.getPath(templateOwnerPath, suffix);
                }
            }
        }

        return toPath;
    }

    private boolean isTemplateAncestor(String toTemplateOwnerPath, String templateOwnerPath)
    {
        TemplateNode toNode = configurationTemplateManager.getTemplateNode(toTemplateOwnerPath);
        TemplateNode localNode = configurationTemplateManager.getTemplateNode(templateOwnerPath);
        while (localNode != null && localNode != toNode)
        {
            localNode = localNode.getParent();
        }

        // Did we exit before we hit the root?
        return localNode != null;
    }

    /**
     * Returns the handle that should be used to reference the record at the
     * given path.  This may not be the handle of the record at that path in a
     * templated scope.
     *
     * @param path the path of the record
     * @return the handle for the record at path, or 0 if there is no such
     *         record
     */
    public long getReferenceHandleForPath(String path)
    {
        Record record = configurationTemplateManager.getRecord(path);
        if (record == null)
        {
            return 0;
        }
        else
        {
            if (record instanceof TemplateRecord && PathUtils.getPathElements(path).length > 2)
            {
                // Inside a templated collection item, pull up to the level
                // where it is first defined.
                TemplateRecord templateRecord = (TemplateRecord) record;
                while (templateRecord.getParent() != null)
                {
                    templateRecord = templateRecord.getParent();
                }

                record = templateRecord;
            }

            return record.getHandle();
        }
    }

    public Configuration resolveReference(String templateOwnerPath, long toHandle, Instantiator instantiator, String indexPath) throws TypeException
    {
        return resolveReference(templateOwnerPath, toHandle, instantiator, configurationTemplateManager.getState().instances, indexPath);
    }

    public Configuration resolveReference(String templateOwnerPath, long toHandle, Instantiator instantiator, InstanceSource cache, String indexPath) throws TypeException
    {
        String toPath = getReferencedPathForHandle(templateOwnerPath, toHandle);
        if (toPath == null)
        {
            throw new TypeException("Broken reference to unknown handle '" + toHandle + "'");
        }

        if (indexPath != null)
        {
            indexReference(indexPath, toPath);
        }

        Configuration instance = cache.get(toPath, true);
        if (instance == null)
        {
            Record record = configurationTemplateManager.getRecord(toPath);
            if (record == null || record.getSymbolicName() == null)
            {
                throw new TypeException("Broken reference to unknown path '" + toPath + "'");
            }

            Type type = typeRegistry.getType(record.getSymbolicName());
            if (type == null)
            {
                throw new TypeException("Reference to unrecognised type '" + record.getSymbolicName() + "'");
            }

            instance = (Configuration) instantiator.instantiate(toPath, false, type, record);
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

    public Collection<Configuration> getReferencableInstances(CompositeType type, String referencingPath)
    {
        Collection<Configuration> instances = new LinkedList<Configuration>();
        String owningScope = configurationPersistenceManager.getClosestOwningScope(type, referencingPath);
        // If the owning scope is a templated object, then we can refer to
        // templated instances.  Otherwise, accept only concrete
        // instances.
        boolean allowTemplated = owningScope != null && configurationTemplateManager.getRecord(owningScope) instanceof TemplateRecord;
        
        for (String path : configurationPersistenceManager.getOwningPaths(type, owningScope))
        {
            configurationTemplateManager.getAllInstances(path, Configuration.class, instances, allowTemplated);
        }

        return instances;
    }

    public void addReferenceCleanupTasks(String path, RecordCleanupTaskSupport result)
    {
        List<String> index = references.get(path);
        if (index != null)
        {
            for (String referencingPath : index)
            {
                ReferenceCleanupTaskProvider provider = getCleanupTaskProvider(referencingPath);
                if (provider != null)
                {
                    RecordCleanupTask task = provider.getTask(path, referencingPath);
                    if (task != null)
                    {
                        result.addCascaded(task);
                    }
                }
            }
        }
    }

    private ReferenceCleanupTaskProvider getCleanupTaskProvider(String referencingPath)
    {
        String parentPath = PathUtils.getParentPath(referencingPath);
        String baseName = PathUtils.getBaseName(referencingPath);

        ComplexType parentType = configurationTemplateManager.getType(parentPath);
        if(parentType instanceof CollectionType)
        {
            baseName = PathUtils.getBaseName(parentPath);
            parentPath = PathUtils.getParentPath(parentPath);
            parentType = configurationTemplateManager.getType(parentPath);
        }

        TypeProperty property = ((CompositeType)parentType).getProperty(baseName);
        Reference ref = property.getAnnotation(Reference.class);
        try
        {
            Class<? extends ReferenceCleanupTaskProvider> providerClass = ClassLoaderUtils.loadAssociatedClass(parentType.getClazz(), ref.cleanupTaskProvider());
            return objectFactory.buildBean(providerClass);
        }
        catch (Exception e)
        {
            LOG.severe("Unable to instantiate reference cleanup task provider of class '" + ref.cleanupTaskProvider() + "': " + e.getMessage(), e);
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

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
