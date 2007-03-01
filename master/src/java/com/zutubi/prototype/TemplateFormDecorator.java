package com.zutubi.prototype;

import com.zutubi.prototype.type.record.TemplateRecord;
import com.zutubi.prototype.config.ConfigurationPersistenceManager;

/**
 *
 *
 */
public class TemplateFormDecorator
{
    private ConfigurationPersistenceManager configurationPersistenceManager;

    private TemplateRecord record;

    public TemplateFormDecorator(TemplateRecord templateRecord)
    {
        this.record = templateRecord;
    }

    public FormDescriptor decorate(FormDescriptor descriptor)
    {
//        String concreteId = record.getOwner();

        for (FieldDescriptor field : descriptor.getFieldDescriptors())
        {
/*
            String id = record.getFieldOwner(field.getName());
            if(!id.equals(concreteId))
            {
                field.getParameters().put("inheritedFrom", id);
            }
*/
            field.getParameters().put("inheritedFrom", "x/y/z");
        }
        return descriptor;
    }

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }
}
