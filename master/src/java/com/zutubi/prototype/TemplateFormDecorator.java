package com.zutubi.prototype;

import com.zutubi.config.annotations.NoInherit;
import com.zutubi.config.annotations.NoOverride;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.TemplateRecord;
import com.zutubi.prototype.model.SelectFieldDescriptor;

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

                // Note that if a field has both noInherit and noOverride,
                // noInherit takes precedence.
                if(fieldHasAnnotation(type, fieldName, NoInherit.class))
                {
                    field.addParameter("noInherit", "true");
                }
                else
                {
                    String ownerId = templateRecord.getOwner(fieldName);
                    if (ownerId != null)
                    {
                        if (!ownerId.equals(templateRecord.getOwner()))
                        {
                            if(fieldHasAnnotation(type, fieldName, NoOverride.class))
                            {
                                // This field should be read-only.
                                field.addParameter("noOverride", "true");
                            }
                            else
                            {
                                field.addParameter("inheritedFrom", ownerId);
                            }
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
        }

        // Select fields may need a special option added to allow the
        // user to not specify a value.
        for (FieldDescriptor field : descriptor.getFieldDescriptors())
        {
            if(field instanceof SelectFieldDescriptor)
            {
                SelectFieldDescriptor select = (SelectFieldDescriptor) field;
                if (!select.getMultiple())
                {
                    Object emptyOption = select.getEmptyOption();
                    if(emptyOption != null)
                    {
                        select.getList().add(0, emptyOption);
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
