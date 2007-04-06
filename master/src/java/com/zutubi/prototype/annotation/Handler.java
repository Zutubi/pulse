package com.zutubi.prototype.annotation;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The handler annotation is used to associate an Annotation instance with its AnnotationHandler.  The system
 * uses the handler to understand what should be done when an instance of the anntotation is encountered.
 *
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Handler
{
    /**
     * The AnnotationHandler class reference.
     *
     * @return an implementation of the AnnotationHandler interface.
     */
    Class<? extends AnnotationHandler> value();
}
