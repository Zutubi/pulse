package com.zutubi.prototype.annotation;

import com.zutubi.prototype.Descriptor;
import com.zutubi.pulse.util.AnnotationUtils;

import java.lang.annotation.Annotation;

/**
 *
 *
 */
public class FieldAnnotationHandler implements AnnotationHandler
{
    public void process(Annotation annotation, Descriptor descriptor)
    {
        // apply annotations..
        descriptor.addAll(AnnotationUtils.collectPropertiesFromAnnotation(annotation));

    }
}
