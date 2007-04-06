package com.zutubi.prototype.annotation;

import com.zutubi.prototype.OptionProvider;
import com.zutubi.pulse.form.FieldType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)

// Annotation handler reference that is used to process 'this' annotation.
@Handler(SelectAnnotationHandler.class)

// This annotation is a form field of type SELECT.
@Field(type = FieldType.SELECT)

/**
 * The select annotation allows you to mark a property for display as a form select field.
 *
 * The contents of the select field are provided by the configured OptionProvider implementation.
 * 
 */
public @interface Select
{
    Class<? extends OptionProvider> optionProvider();

    /**
     * The DEFAULT_size indicates that, by default, no size value will be rendered.  For convenience, the
     * rendered default (as defined by the html form spec) and the default value here are the same.
     */
    public static final int DEFAULT_size = 1;

    /**
     * The size property defined the number of options that will be visible in the select widget at the same
     * time.
     * 
     * @return the number of visible options in the select widget.
     */
    public int size() default DEFAULT_size;
}
