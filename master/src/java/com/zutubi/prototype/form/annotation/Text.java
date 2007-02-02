package com.zutubi.prototype.form.annotation;

import com.zutubi.pulse.form.FieldType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * <class-comment/>
 */
@Handler(FieldAnnotationHandler.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)

@Field(type = FieldType.TEXT)
public @interface Text
{
    public static final int DEFAULT_size = 0;

    public int size() default DEFAULT_size;
}
