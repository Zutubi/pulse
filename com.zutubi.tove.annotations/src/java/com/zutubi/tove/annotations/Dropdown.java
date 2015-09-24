package com.zutubi.tove.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Marks a property for display as a form dropdown box field.
 *
 * The contents of the field are provided by the configured OptionProvider implementation.
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Field(type = FieldType.DROPDOWN)
@Handler(className = DefaultAnnotationHandlers.OPTION)
public @interface Dropdown
{
    String optionProvider() default "";
}
