package com.zutubi.tove.annotations;

import com.zutubi.validation.annotations.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks the field as a name.  A valid name: 
 * <ul>
 * <li>must not begin or end with a whitespace character</li>
 * <li>must not contain the '/', '\' or '$' characters. </li>
 * </ul>
 *
 */
@Constraint("com.zutubi.tove.validation.NameValidator")
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Name
{
    static final String DEFAULT_defaultKeySuffix = "";

    static final boolean DEFAULT_shortCircuit = true;

    String defaultKeySuffix() default DEFAULT_defaultKeySuffix;

    boolean shortCircuit() default DEFAULT_shortCircuit;
}
