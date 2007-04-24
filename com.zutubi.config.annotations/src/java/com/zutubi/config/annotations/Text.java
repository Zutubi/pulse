package com.zutubi.config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)

// This annotation is a form field of type TEXT.
@Field(type = FieldType.TEXT)

/**
 * The text annotation allows you to mark a property for display as a simple form text field.
 *
 */
@Handler(className = DefaultAnnotationHandlers.FIELD)
public @interface Text
{
    /**
     * The DEFAULT_size indicates that, by default, no size value will be rendered.
     */
    public static final int DEFAULT_size = 0;

    /**
     * The size of the rendered text field.
     * 
     * @return number of columns to be displayed.
     */
    public int size() default DEFAULT_size;
}
