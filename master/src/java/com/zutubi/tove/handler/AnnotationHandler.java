package com.zutubi.tove.handler;

import com.zutubi.tove.Descriptor;
import com.zutubi.tove.type.CompositeType;

import java.lang.annotation.Annotation;

/**
 *
 *
 */
public interface AnnotationHandler
{
    void process(CompositeType annotatedType, Annotation annotation, Descriptor descriptor) throws Exception;
}
