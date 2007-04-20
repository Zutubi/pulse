package com.zutubi.prototype.annotation;

import com.zutubi.prototype.annotation.FieldType;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})

// This annotation is a form field of type TEXTAREA.
@Field(type = FieldType.TEXTAREA)

/**
 * The TextArea annotation is used to mark a property as being rendered using a text field.
 *
 */
public @interface TextArea
{
    /**
     * The default rows value indicating that no rows value should be rendered.
     */
    public static final int DEFAULT_rows = 0;

    /**
     * The number of rows to be rendered for the text area.
     *
     * @return the integer row count.  Returning the default value indicates that no row count
     * will be rendered.
     */
    public int rows() default DEFAULT_rows;

    /**
     * The default cols value indicating that no cols value should be rendered.
     */
    public static final int DEFAULT_cols = 0;

    /**
     * The number of columns to be rendered for the text area.
     *
     * @return the integer column count.  Returning the default value indicates that no column count
     * will be rendered.
     */
    public int cols() default DEFAULT_cols;

}
