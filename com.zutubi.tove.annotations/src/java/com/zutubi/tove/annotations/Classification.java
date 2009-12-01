package com.zutubi.tove.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to classify a configuration type.  The classification is added to the
 * icon class for the type in the UI.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Classification
{
    /**
     * @return static class to use for single composites, empty for the default
     */
    String single() default "";

    /**
     * @return static class to use for collections, empty for the default
     */
    String collection() default "";
}
