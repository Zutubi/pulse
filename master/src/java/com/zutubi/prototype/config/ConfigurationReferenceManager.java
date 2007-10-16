package com.zutubi.prototype.config;

import com.zutubi.config.annotations.Reference;
import com.zutubi.prototype.config.cleanup.DefaultReferenceCleanupTaskProvider;
import com.zutubi.prototype.config.cleanup.RecordCleanupTask;
import com.zutubi.prototype.config.cleanup.RecordCleanupTaskSupport;
import com.zutubi.prototype.config.cleanup.ReferenceCleanupTaskProvider;
import com.zutubi.prototype.type.*;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.prototype.type.record.TemplateRecord;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.util.ClassLoaderUtils;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import java.util.*;

/**
 * Manages the resolution, maintenance and cleanup of references between
 * configuration instances.
 */
public class ConfigurationReferenceManager
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

    public String getPathForHandle(long handle)
    {
        return recordManager.getPathForHandle(handle);
    }

    /**
     * Returns the handle for the record at the given path.
     *
     * @param path the path of the record
     * @return the handle for the record at path, or 0 if there is no such
     *         record
     */
    public long getHandleForPath(String path)
    {
        Record record = recordManager.select(path);
        if(record == null)
        {
            return 0;
        }
        else
        {
            return record.getHandle();
        }
    }

    public Configuration resolveReference(String fromPath, long toHandle, Instantiator instantiator) throws TypeException
    {
        String toPath = recordManager.getPathForHandle(toHandle);
        if(toPath == null)
        {
            throw new TypeException("Broken reference to unknown handle '" + toHandle + "'");
        }

        if (fromPath != null)
        {
            indexReference(fromPath, toPath);
        }
        
        Configuration instance = configurationTemplateManager.getInstance(toPath);
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
            configurationTemplateManager.getAllInstances(path, instances, allowTemplated);
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
