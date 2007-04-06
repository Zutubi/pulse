package com.zutubi.prototype.annotation;

import com.zutubi.pulse.form.FieldType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)

// Annotation handler reference that is used to process 'this' annotation. 
@Handler(FieldAnnotationHandler.class)

// This annotation is a form field of type TEXT.
@Field(type = FieldType.TEXT)

/**
 * The text annotation allows you to mark a property for display as a simple form text field.
 *
 */
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
