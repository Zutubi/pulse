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

@Field(fieldType = "mock")
public @interface MockField
{
    public static final String DEFAULT_a = "A";
    public static final int DEFAULT_b = 190;

    String a() default DEFAULT_a;

    int b() default DEFAULT_b;
}
