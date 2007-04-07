package com.zutubi.prototype.annotation;

import com.zutubi.prototype.Descriptor;
import com.zutubi.prototype.FieldDescriptor;
import com.zutubi.prototype.OptionProvider;
import com.zutubi.prototype.type.ReferenceType;

import java.lang.annotation.Annotation;

/**
 * Handler for processing reference properties.  Adds the list to select from
 * to the descriptor.
 */
public class ReferenceAnnotationHandler extends OptionAnnotationHandler
{
    public void process(Annotation annotation, Descriptor descriptor) throws Exception
    {
        super.process(annotation, descriptor);
        FieldDescriptor field = (FieldDescriptor) descriptor;
        ReferenceType type = (ReferenceType) field.getProperty().getType().getTargetType();
        if(type.isMultiple())
        {
            descriptor.addParameter("multiple", true);
        }
    }

    protected Class<? extends OptionProvider> getOptionProviderClass(Annotation annotation)
    {
        return ((Reference)annotation).optionProvider();
    }
}
