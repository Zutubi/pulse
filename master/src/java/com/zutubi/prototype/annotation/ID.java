package com.zutubi.prototype.annotation;

import com.zutubi.validation.annotations.Constraint;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A marker annotation that defines the id field for the object.  The id field is used to uniquely
 * identify an object instance.
 *
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint({"com.zutubi.validation.validators.RequiredValidator", "com.zutubi.validation.validators.NameValidator", "com.zutubi.prototype.validation.UniqueNameValidator"})
public @interface ID
{
}
