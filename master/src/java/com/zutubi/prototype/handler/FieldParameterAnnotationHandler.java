package com.zutubi.prototype.handler;

import com.zutubi.config.annotations.Parameter;
import com.zutubi.prototype.Descriptor;
import com.zutubi.prototype.FieldDescriptor;
import com.zutubi.prototype.type.CompositeType;

import java.lang.annotation.Annotation;

/**
 * Handler for the {@link com.zutubi.config.annotations.FieldAction} annotation.
 */
public class FieldParameterAnnotationHandler implements AnnotationHandler
{
    public void process(CompositeType annotatedType, Annotation annotation, Descriptor descriptor) throws Exception
    {
        FieldDescriptor fieldDescriptor = (FieldDescriptor) descriptor;
        Parameter parameter = (Parameter) annotation;
        fieldDescriptor.addParameter(parameter.name(), parameter.value());
    }
}
