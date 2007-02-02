package com.zutubi.prototype.form.annotation;

import com.zutubi.prototype.form.FieldDescriptor;
import com.zutubi.prototype.form.Descriptor;

import java.lang.annotation.Annotation;

/**
 *
 *
 */
public interface AnnotationHandler
{
    void process(Annotation annotation, Descriptor descriptor);
}
