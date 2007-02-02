package com.zutubi.prototype.form.annotation;

import com.zutubi.prototype.form.FieldDescriptor;
import com.zutubi.prototype.form.Descriptor;
import com.zutubi.validation.bean.AnnotationUtils;

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
//        AnnotationUtils.setPropertiesFromAnnotation(annotation, descriptor);

        descriptor.getParameters().putAll(AnnotationUtils.collectPropertiesFromAnnotation(annotation));

    }
}
