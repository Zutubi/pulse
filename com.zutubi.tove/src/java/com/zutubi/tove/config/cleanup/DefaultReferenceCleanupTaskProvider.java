package com.zutubi.tove.config.cleanup;

import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.type.*;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.validation.annotations.Required;

/**
 */
public class DefaultReferenceCleanupTaskProvider implements ReferenceCleanupTaskProvider
{
    private ConfigurationTemplateManager configurationTemplateManager;
    private RecordManager recordManager;

    public RecordCleanupTask getTask(String deletedPath, String referencingPath)
    {
        // Default behaviour:
        //   - if referencing path is a simple property
        //     - if not required, null it out
        //     - if required, delete the parent of the referencing path
        //   - else if referencing path is a collection, remove from the
        //     collection
        String parentPath = PathUtils.getParentPath(referencingPath);
        String baseName = PathUtils.getBaseName(referencingPath);

        ComplexType parentType = configurationTemplateManager.getType(parentPath);
        if(parentType instanceof CollectionType)
        {
            baseName = PathUtils.getBaseName(parentPath);
            parentPath = PathUtils.getParentPath(parentPath);
            parentType = configurationTemplateManager.getType(parentPath);
        }

        TypeProperty property = ((CompositeType) parentType).getProperty(baseName);
        if(property.getType() instanceof ReferenceType)
        {
            if (configurationTemplateManager.existsInTemplateParent(referencingPath))
            {
                // Inherited single references cleaned in parent.
                return null;
            }
            else
            {
                if(property.getAnnotation(Required.class) != null)
                {
                    return configurationTemplateManager.getCleanupTasks(parentPath);
                }
                else
                {
                    return new NullifyReferenceCleanupTask(referencingPath);
                }
            }
        }
        else
        {
            return new RemoveReferenceCleanupTask(referencingPath, recordManager.select(deletedPath).getHandle());
        }
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}
