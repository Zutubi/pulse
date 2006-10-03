package com.zutubi.pulse.form.descriptor.annotation;

import com.zutubi.pulse.form.FieldType;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <class-comment/>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)

@Field(fieldType = FieldType.TEXT)
public @interface Text
{
    public static final int DEFAULT_size = 0;

    public int size() default DEFAULT_size;
}
