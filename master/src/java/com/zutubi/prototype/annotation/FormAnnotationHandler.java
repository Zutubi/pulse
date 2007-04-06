package com.zutubi.prototype.annotation;

import com.zutubi.prototype.Descriptor;
import com.zutubi.pulse.util.AnnotationUtils;

import java.lang.annotation.Annotation;

/**
 *
 *
 */
public class FormAnnotationHandler implements AnnotationHandler
{
    public void process(Annotation annotation, Descriptor descriptor)
    {
        // Collect all of the annotations fields in a map and add them to the descriptor.
        descriptor.addAll(AnnotationUtils.collectPropertiesFromAnnotation(annotation));
    }
}
