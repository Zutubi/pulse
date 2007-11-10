package com.zutubi.config.annotations;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used to classify a configuration type.  The classification is added to the
 * icon class for the type in the UI.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Classification
{
    String single() default "";
    String collection() default "";
}
