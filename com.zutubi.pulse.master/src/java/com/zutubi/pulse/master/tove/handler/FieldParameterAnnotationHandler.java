package com.zutubi.pulse.master.tove.handler;

import com.zutubi.pulse.master.rest.model.forms.FieldModel;
import com.zutubi.pulse.master.tove.model.Descriptor;
import com.zutubi.pulse.master.tove.model.FieldDescriptor;
import com.zutubi.tove.annotations.Parameter;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeProperty;

import java.lang.annotation.Annotation;

/**
 * Handler for the {@link com.zutubi.tove.annotations.FieldAction} annotation.
 */
public class FieldParameterAnnotationHandler implements AnnotationHandler
{
    @Override
    public void process(CompositeType annotatedType, Annotation annotation, Descriptor descriptor) throws Exception
    {
        FieldDescriptor fieldDescriptor = (FieldDescriptor) descriptor;
        Parameter parameter = (Parameter) annotation;
        fieldDescriptor.addParameter(parameter.name(), parameter.value());
    }

    @Override
    public void process(CompositeType annotatedType, TypeProperty property, Annotation annotation, FieldModel field) throws Exception
    {
        Parameter parameter = (Parameter) annotation;
        field.addParameter(parameter.name(), parameter.value());
    }
}
