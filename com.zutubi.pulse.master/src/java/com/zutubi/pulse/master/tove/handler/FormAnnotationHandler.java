package com.zutubi.pulse.master.tove.handler;

import com.zutubi.config.annotations.Form;
import com.zutubi.pulse.master.tove.model.Descriptor;
import com.zutubi.pulse.master.tove.model.FormDescriptor;
import com.zutubi.tove.type.CompositeType;
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
