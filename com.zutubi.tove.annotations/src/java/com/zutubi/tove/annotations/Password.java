package com.zutubi.tove.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * The password annotation allows you to mark a property for display as a simple form password field.
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Field(type = FieldType.PASSWORD)
@Handler(className = DefaultAnnotationHandlers.FIELD)
public @interface Password
{
    /**
     * The DEFAULT_size indicates that, by default, no size value will be rendered.
     */
    int DEFAULT_size = 0;

    /**
     * The size of the rendered password field.
     *
     * @return number of columns to be displayed.
     */
    int size() default DEFAULT_size;
}
