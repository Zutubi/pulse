package com.zutubi.prototype.form.annotation;


import com.zutubi.pulse.form.FieldType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <class-comment/>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)

@Field(type = FieldType.PASSWORD)
public @interface Password
{
}
