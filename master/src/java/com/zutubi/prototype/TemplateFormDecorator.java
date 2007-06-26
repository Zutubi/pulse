package com.zutubi.prototype;

import com.zutubi.config.annotations.NoInherit;
import com.zutubi.config.annotations.NoOverride;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.TemplateRecord;

import java.lang.annotation.Annotation;

/**
 * Decorates form fields with templating information if there is such
 * information available (i.e. for templated scopes).
 */
public class TemplateFormDecorator
{
    private Record record;

    public TemplateFormDecorator(Record record)
    {
        this.record = record;
    }

    public FormDescriptor decorate(FormDescriptor descriptor)
    {
        if (record != null && record instanceof TemplateRecord)
        {
            TemplateRecord templateRecord = (TemplateRecord) record;
            TemplateRecord parentRecord = templateRecord.getParent();
            CompositeType type = (CompositeType) templateRecord.getType();
            
            for (FieldDescriptor field : descriptor.getFieldDescriptors())
            {
                String fieldName = field.getName();

                // If a field has both noInherit and noOverride, noInherit
                // takes precedence.
                if(fieldHasAnnotation(type, fieldName, NoInherit.class))
                {
                    field.addParameter("noInherit", "true");
                    continue;
                }

                if(fieldHasAnnotation(type, fieldName, NoOverride.class))
                {
                    // This field should be read-only.
                    field.addParameter("noOverride", "true");
                    continue;
                }

                // Field follows normal inheritance rules.  Decorate it if it
                // inherits or overrides a value.
                String ownerId = templateRecord.getOwner(fieldName);
                if (ownerId != null)
                {
                    if (!ownerId.equals(templateRecord.getOwner()))
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

    private boolean fieldHasAnnotation(CompositeType type, String fieldName, Class<? extends Annotation> annotationClass)
    {
        return type.getProperty(fieldName).getAnnotation(annotationClass) != null;
    }
}
