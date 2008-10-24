package com.zutubi.pulse.master.tove.handler;

import com.zutubi.config.annotations.Parameter;
import com.zutubi.pulse.master.tove.model.Descriptor;
import com.zutubi.pulse.master.tove.model.FieldDescriptor;
import com.zutubi.tove.type.CompositeType;

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
