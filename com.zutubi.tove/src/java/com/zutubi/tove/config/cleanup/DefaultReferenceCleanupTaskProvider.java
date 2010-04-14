package com.zutubi.tove.config.cleanup;

import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.type.*;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.tove.type.record.TemplateRecord;
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
            Record referencingRecord = configurationTemplateManager.getRecord(parentPath);
            boolean hasTemplateParent = false;
            if (referencingRecord instanceof TemplateRecord)
            {
                TemplateRecord templateRecord = (TemplateRecord) referencingRecord;
                if (!templateRecord.getOwner(baseName).equals(templateRecord.getOwner()))
                {
                    // Inherited single references cleaned in parent.
                    return null;
                }

                hasTemplateParent = templateRecord.getParent() != null;
            }

            if(property.getAnnotation(Required.class) != null)
            {
                return configurationTemplateManager.getCleanupTasks(parentPath);
            }
            else
            {
                return new NullifyReferenceCleanupTask(referencingPath, hasTemplateParent);
            }
        }
        else
        {
            String[] inheritedValues = null;
            Record referencingRecord = configurationTemplateManager.getRecord(parentPath);
            if (referencingRecord instanceof TemplateRecord)
            {
                TemplateRecord templateParent = ((TemplateRecord) referencingRecord).getParent();
                if (templateParent != null)
                {
                    inheritedValues = (String[]) templateParent.get(baseName);
                }
            }
            
            return new RemoveReferenceCleanupTask(referencingPath, recordManager.select(deletedPath).getHandle(), inheritedValues);
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
