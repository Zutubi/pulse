package com.zutubi.tove.annotations;

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
 */
@Handler(className = DefaultAnnotationHandlers.FIELD)
public @interface Text
{
    /**
     * The DEFAULT_size indicates that, by default, no size value will be rendered.
     */
    int DEFAULT_size = 0;

    boolean DEFAULT_readOnly = false;

    /**
     * The size of the rendered text field.
     * 
     * @return number of columns to be displayed.
     */
    int size() default DEFAULT_size;

    /**
     * Mark the field as readonly for the UI.
     *
     * @return true if the field is readonly, false otherwise.
     */
    boolean readOnly() default DEFAULT_readOnly;
}
