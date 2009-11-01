package com.zutubi.tove.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)

// This annotation is a form field of type SELECT.
@Field(type = FieldType.SELECT)

/**
 * The select annotation allows you to mark a property for display as a form select field.
 *
 * The contents of the select field are provided by the configured OptionProvider implementation.
 * 
 */
@Handler(className = DefaultAnnotationHandlers.SELECT)
public @interface Select
{
    String optionProvider() default "";

    /**
     * The DEFAULT_size indicates that, by default, no size value will be rendered.  For convenience, the
     * rendered default (as defined by the html form spec) and the default value here are the same.
     */
    public static final int DEFAULT_size = 1;
    /**
     * By default, users are restricted to the presented options.
     */
    public static final boolean DEFAULT_editable = false;
    /**
     * Options are loaded eagerly by default.
     */
    public static final boolean DEFAULT_lazy = false;

    /**
     * The size property defined the number of options that will be visible in the select widget at the same
     * time.
     * 
     * @return the number of visible options in the select widget.
     */
    public int size() default DEFAULT_size;

    /**
     * If true, the user may manually enter a value rather than being
     * restricted to the available options.
     *
     * @return whether the field is editable by the user
     */
    public boolean editable() default DEFAULT_editable;

    /**
     * If true, options will be loaded lazily when the user drops down the
     * list.
     *
     * @return whether the field options are lazily loaded
     */
    public boolean lazy() default DEFAULT_lazy;
}
