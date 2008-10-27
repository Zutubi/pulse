package com.zutubi.tove.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to add a custom parameter to a field.  The parameter is available to
 * the field template.
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Handler(className = DefaultAnnotationHandlers.FIELD_PARAMETER)
public @interface Parameter
{
    /**
     * @return the parameter name
     */
    public String name();

    /**
     * @return the parameter value.
     */
    public String value() default "";
}
