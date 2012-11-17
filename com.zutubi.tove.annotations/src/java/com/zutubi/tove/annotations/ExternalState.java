package com.zutubi.tove.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a property that stores a handle to external state.  This property
 * must be of type long - the value of which is a handle or identifier for
 * the external state.
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Internal @NoInherit
public @interface ExternalState
{
}
