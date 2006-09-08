package com.zutubi.pulse.form.descriptor.annotation;

import com.zutubi.pulse.form.descriptor.Descriptor;

import java.lang.annotation.Annotation;

/**
 * <class-comment/>
 */
public interface DescriptorAnnotationHandler<T extends Annotation,X extends Descriptor>
{
    public X decorateFromAnnotation(T annotation, X descriptor);
}