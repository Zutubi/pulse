package com.zutubi.pulse.form.descriptor.annotation;

import com.zutubi.pulse.form.FieldType;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @deprecated
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)

@Field(fieldType = FieldType.RADIO)
public @interface Radio
{
    public static final String DEFAULT_value = "";

    String[] list();

    String value() default DEFAULT_value;
}
