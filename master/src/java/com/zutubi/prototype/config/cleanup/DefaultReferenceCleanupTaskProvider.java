package com.zutubi.prototype.config.cleanup;

import com.zutubi.prototype.config.ConfigurationTemplateManager;
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
    private ConfigurationTemplateManager configurationTemplateManager;
    private RecordManager recordManager;

    public RecordCleanupTask getAction(String deletedPath, String referencingPath)
    {
        // Default behaviour:
        //   - if referencing path is a simple property
        //     - if not required, null it out
        //     - if required, delete the parent of the referencing path
        //   - else if referencing path is a collection, remove from the
        //     collection
        String parentPath = PathUtils.getParentPath(referencingPath);
        String baseName = PathUtils.getBaseName(referencingPath);

        CompositeType parentType = (CompositeType) configurationTemplateManager.getType(parentPath);
        TypeProperty property = parentType.getProperty(baseName);
        if(property.getType() instanceof ReferenceType)
        {
            if(property.getAnnotation(Required.class) != null)
            {
                return configurationTemplateManager.getCleanupTasks(parentPath);
            }
            else
            {
                return new NullifyReferenceCleanupTask(referencingPath, recordManager);
            }
        }
        else
        {
            return new RemoveReferenceCleanupTask(deletedPath, referencingPath, recordManager);
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
