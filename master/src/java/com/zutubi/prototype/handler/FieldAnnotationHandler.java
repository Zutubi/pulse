package com.zutubi.prototype.handler;

import com.zutubi.prototype.Descriptor;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.util.AnnotationUtils;

import java.lang.annotation.Annotation;

/**
 *
 *
 */
public class FieldAnnotationHandler implements AnnotationHandler
{
    public void process(CompositeType annotatedType, Annotation annotation, Descriptor descriptor) throws Exception
    {
        // Collect all of the annotations fields in a map and add them to the descriptor.
        descriptor.addAll(AnnotationUtils.collectPropertiesFromAnnotation(annotation));
    }
}
