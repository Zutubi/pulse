package com.zutubi.tove.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Associates an explicit state display class for some configuration class.
 * Normally state display classes are looked up by the convention that they
 * use the same name as the configuration class with a "StateDisplay" suffix.
 * Where this is not desired, however, this annotation may be used to link an
 * arbitrary class.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface StateDisplay
{
    /**
     * Specifies the name of the state display class, either as a simple name
     * if the class is in the same package as the configuration class, or as a
     * fully-qualified name.
     *
     * @return name of the state displat class
     */
    String value();
}
