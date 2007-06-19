package com.zutubi.prototype;

import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.TemplateRecord;

/**
 * Decorates form fields with templating information if there is such
 * information available (i.e. for templated scopes).
 */
public class TemplateFormDecorator
{
    private String path;
    private Record record;
    private ConfigurationTemplateManager configurationTemplateManager;

    public TemplateFormDecorator(String path, Record record)
    {
        this.path = path;
        this.record = record;
    }

    public FormDescriptor decorate(FormDescriptor descriptor)
    {
        if (record != null && record instanceof TemplateRecord)
        {
            String concreteId = configurationTemplateManager.getOwner(path);
            TemplateRecord templateRecord = (TemplateRecord) record;
            TemplateRecord parentRecord = templateRecord.getParent();

            for (FieldDescriptor field : descriptor.getFieldDescriptors())
            {
                String fieldName = field.getName();
                String ownerId = templateRecord.getOwner(fieldName);
                if (ownerId != null)
                {
                    if (!ownerId.equals(concreteId))
                    {
                        field.addParameter("inheritedFrom", ownerId);
                    }
                    else if(parentRecord != null)
                    {
                        // Check for override
                        String parentOwnerId = parentRecord.getOwner(fieldName);
                        if(parentOwnerId != null)
                        {
                            field.addParameter("overriddenOwner", parentOwnerId);
                            field.addParameter("overriddenValue", parentRecord.get(fieldName));
                        }
                    }
                }
            }
        }

        return descriptor;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}
