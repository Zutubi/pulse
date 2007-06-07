package com.zutubi.prototype.config;

import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.ReferenceType;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.validation.annotations.Required;

/**
 */
public class DefaultReferenceCleanupTaskProvider implements ReferenceCleanupTaskProvider
{
    private ConfigurationPersistenceManager configurationPersistenceManager;
    private ConfigurationReferenceManager configurationReferenceManager;
    private RecordManager recordManager;

    public ReferenceCleanupTask getAction(String deletedPath, String referencingPath)
    {
        // Default behaviour:
        //   - if referencing path is a simple property
        //     - if not required, null it out
        //     - if required, delete the parent of the referencing path
        //   - else if referencing path is a collection, remove from the
        //     collection
        String parentPath = PathUtils.getParentPath(referencingPath);
        String baseName = PathUtils.getBaseName(referencingPath);

        CompositeType parentType = (CompositeType) configurationPersistenceManager.getType(parentPath);
        TypeProperty property = parentType.getProperty(baseName);
        if(property.getType() instanceof ReferenceType)
        {
            if(property.getAnnotation(Required.class) != null)
            {
                return configurationReferenceManager.getCleanupTasks(parentPath);
            }
            else
            {
                return new NullifyCleanupTask(referencingPath, recordManager);
            }
        }
        else
        {
            return new RemoveCleanupTask(deletedPath, referencingPath, recordManager);
        }
    }

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }

    public void setConfigurationReferenceManager(ConfigurationReferenceManager configurationReferenceManager)
    {
        this.configurationReferenceManager = configurationReferenceManager;
    }
}
