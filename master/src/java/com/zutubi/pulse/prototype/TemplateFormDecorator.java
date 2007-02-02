package com.zutubi.pulse.prototype;

import com.zutubi.pulse.form.descriptor.FieldDescriptor;
import com.zutubi.pulse.form.descriptor.FormDescriptor;

/**
 * <class comment/>
 */
public class TemplateFormDecorator
{
    private TemplateRecord record;

    public TemplateFormDecorator(TemplateRecord record)
    {
        this.record = record;
    }

    public FormDescriptor decorate(FormDescriptor descriptor)
    {
        String concreteId = record.getOwner();

        for (FieldDescriptor field : descriptor.getFieldDescriptors())
        {
            String id = record.getFieldOwner(field.getName());
            if(!id.equals(concreteId))
            {
                field.getParameters().put("inheritedFrom", id);
            }
        }
        return descriptor;
    }
}
