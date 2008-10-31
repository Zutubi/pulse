package com.zutubi.tove.annotations;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used to add a custom script for a form field.  The script template is
 * included just after the field, allowing custom manipulation.
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Handler(className = DefaultAnnotationHandlers.FIELD_SCRIPT)
public @interface FieldScript
{
    /**
     * @return the name of the template used to render the link's associated
     * code.
     */
    public String template() default "";
}
