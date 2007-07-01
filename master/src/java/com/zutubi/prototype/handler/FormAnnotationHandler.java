package com.zutubi.prototype.handler;

import com.zutubi.config.annotations.Form;
import com.zutubi.prototype.Descriptor;
import com.zutubi.prototype.FormDescriptor;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.util.AnnotationUtils;

import java.lang.annotation.Annotation;

/**
 *
 *
 */
public class FormAnnotationHandler implements AnnotationHandler
{
    public void process(CompositeType annotatedType, Annotation annotation, Descriptor descriptor)
    {
        Form form = (Form) annotation;
        FormDescriptor formDescriptor = (FormDescriptor) descriptor;
        formDescriptor.setActions(form.actions());
        
        // Collect all of the annotations fields in a map and add them to the descriptor.
        descriptor.addAll(AnnotationUtils.collectPropertiesFromAnnotation(annotation));
    }
}
