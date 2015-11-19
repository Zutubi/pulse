package com.zutubi.tove.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)

/**
 * The field annotation allows a field / method to be annotated with details on how it should be
 * rendered in the form.  This is ideal for both overriding the default system behaviour and for
 * making it explicit.
 *  
 */
@Handler(className = DefaultAnnotationHandlers.FIELD)
public @interface Field
{
    /**
     * The field type is used to define how the field will be rendered.
     *
     * @return a valid type identifier
     */
    String type();
}