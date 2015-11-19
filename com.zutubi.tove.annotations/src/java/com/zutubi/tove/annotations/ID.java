package com.zutubi.tove.annotations;

import com.zutubi.validation.annotations.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A marker annotation that defines the id field for the object.  The id field is used to uniquely
 * identify an object instance.
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint({"com.zutubi.tove.validation.NameValidator", "com.zutubi.tove.validation.UniqueNameValidator"})
@NoOverride
public @interface ID
{
}
