package com.zutubi.tove.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * String lists are handled by a custom widget that allows the user to enter
 * strings into a list directly within a form.
 */
@Target({ ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Field(type = FieldType.STRING_LIST)
@Handler(className = DefaultAnnotationHandlers.FIELD)
public @interface StringList
{
}
