package com.zutubi.prototype.form.annotation;

import com.zutubi.prototype.form.Descriptor;
import com.zutubi.validation.bean.AnnotationUtils;

import java.lang.annotation.Annotation;

/**
 *
 *
 */
public class FormAnnotationHandler implements AnnotationHandler
{
    public void process(Annotation annotation, Descriptor descriptor)
    {
        // apply annotations..
        descriptor.getParameters().putAll(AnnotationUtils.collectPropertiesFromAnnotation(annotation));

    }
}
