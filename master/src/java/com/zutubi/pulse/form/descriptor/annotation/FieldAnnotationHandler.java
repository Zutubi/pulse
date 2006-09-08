package com.zutubi.pulse.form.descriptor.annotation;

import com.zutubi.pulse.form.descriptor.FieldDescriptor;

import java.util.Map;

/**
 * <class-comment/>
 */
public class FieldAnnotationHandler extends AbstractAnnotationHandler implements DescriptorAnnotationHandler<Field, FieldDescriptor>
{
    public FieldAnnotationHandler()
    {
    }

    public FieldDescriptor decorateFromAnnotation(Field annotation, FieldDescriptor descriptor)
    {
        Map<String, Object> parameters = collectPropertiesFromAnnotation(annotation);
        descriptor.getParameters().putAll(parameters);

        setPropertiesFromAnnotation(annotation, descriptor);

        return descriptor;
    }

}
