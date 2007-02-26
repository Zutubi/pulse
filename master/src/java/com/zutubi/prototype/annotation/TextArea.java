package com.zutubi.prototype.annotation;

import com.zutubi.pulse.form.FieldType;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)

@Field(type = FieldType.TEXTAREA)
public @interface TextArea
{
}
