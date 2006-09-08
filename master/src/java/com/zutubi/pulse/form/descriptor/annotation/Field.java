package com.zutubi.pulse.form.descriptor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <class-comment/>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@DescriptorAnnotation(FieldAnnotationHandler.class)
public @interface Field
{
    public boolean required() default false;
}