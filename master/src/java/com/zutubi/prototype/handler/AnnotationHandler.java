package com.zutubi.prototype.handler;

import com.zutubi.prototype.Descriptor;
import com.zutubi.prototype.type.CompositeType;

import java.lang.annotation.Annotation;

/**
 *
 *
 */
public interface AnnotationHandler
{
    void process(CompositeType annotatedType, Annotation annotation, Descriptor descriptor) throws Exception;
}
