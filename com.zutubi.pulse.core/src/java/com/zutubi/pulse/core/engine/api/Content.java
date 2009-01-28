package com.zutubi.pulse.core.engine.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to mark a text property as settable using the content contained within
 * XML tags.  When binding XML to objects, if text content is found between
 * tags it will be used to set a property with this annotation (if any).
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Content
{
}