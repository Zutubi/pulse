package com.zutubi.prototype.annotation;

import com.zutubi.pulse.form.FieldType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * The text annotation represents a simple form text field.
 *
 */
@Handler(FieldAnnotationHandler.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)

@Field(type = FieldType.TEXT)
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
