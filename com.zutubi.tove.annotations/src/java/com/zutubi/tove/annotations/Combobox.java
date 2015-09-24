package com.zutubi.tove.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a property for display as a combobox (editable dropdown list).
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Field(type = FieldType.COMBOBOX)
@Handler(className = DefaultAnnotationHandlers.OPTION)
public @interface Combobox
{
    String optionProvider() default "";

    /**
     * If true, options will be loaded lazily when the user drops down the list.
     *
     * @return whether the field options are lazily loaded
     */
    boolean lazy() default false;
}
