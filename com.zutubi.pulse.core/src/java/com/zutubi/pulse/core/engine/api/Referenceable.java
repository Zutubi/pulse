package com.zutubi.pulse.core.engine.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to mark a configuration type as referenceable - i.e. the instance
 * adds a value (possibly itself) to the scope when loading so that it may be
 * referenced by the $(name) syntax.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Referenceable
{
    /**
     * Defines the property of the instance to use as the reference name.  By
     * default this is the "name" property, and it is strongly recommended to
     * use this default.
     *
     * @return the name of the property that contains the reference name
     */
    public String nameProperty() default "name";

    /**
     * Defines the property of the instance to use as the reference value.  If
     * not specified, the instance itself is used as the value.
     *
     * @return the name of the property that contains the reference value
     */
    public String valueProperty() default "";
}