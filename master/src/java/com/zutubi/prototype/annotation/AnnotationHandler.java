package com.zutubi.prototype.annotation;

import com.zutubi.prototype.Descriptor;

import java.lang.annotation.Annotation;

/**
 *
 *
 */
public interface AnnotationHandler
{
    void process(Annotation annotation, Descriptor descriptor);
}
