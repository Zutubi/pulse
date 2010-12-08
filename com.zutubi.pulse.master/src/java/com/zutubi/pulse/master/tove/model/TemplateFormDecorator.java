package com.zutubi.pulse.master.tove.model;

import com.zutubi.tove.annotations.NoInherit;
import com.zutubi.tove.annotations.NoOverride;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.record.TemplateRecord;

import java.lang.annotation.Annotation;

/**
 * Decorates form fields with templating information if there is such
 * information available (i.e. for templated scopes).
 */
public class TemplateFormDecorator
{
    private TemplateRecord templateRecord;

    public TemplateFormDecorator(TemplateRecord record)
    {
        this.templateRecord = record;
    }

    public FormDescriptor decorate(FormDescriptor descriptor)
    {
        if (templateRecord != null)
        {
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
            if(field instanceof OptionFieldDescriptor)
            {
                OptionFieldDescriptor optionFieldDescriptor = (OptionFieldDescriptor) field;
                if (!optionFieldDescriptor.getMultiple())
                {
                    Object emptyOption = optionFieldDescriptor.getEmptyOption();
                    if(emptyOption != null)
                    {
                        optionFieldDescriptor.getList().add(0, emptyOption);
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
