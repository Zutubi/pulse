package com.zutubi.prototype.annotation;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)

// Annotation handler reference that is used to process 'this' annotation.
@Handler(FieldAnnotationHandler.class)

/**
 * The field annotation allows a field / method to be annotated with details on how it should be
 * rendered in the form.  This is ideal for both overriding the default system behaviour and for
 * making it explicit.
 *  
 */
public @interface Field
{
    /**
     * The field type is used to define how the field will be rendered.
     *
     * @return a valid type identifier
     */
    public String type();
}