package com.zutubi.pulse.form.descriptor.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <class-comment/>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DescriptorAnnotation
{
    Class<? extends DescriptorAnnotationHandler> value();
}
