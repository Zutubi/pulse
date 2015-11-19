package com.zutubi.validation.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as required.  If the field is unset or empty, it is
 * considered invalid.
 */
@Constraint("com.zutubi.validation.validators.RequiredValidator")
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Required
{
    String DEFAULT_defaultKeySuffix = "";

    boolean DEFAULT_shortCircuit = true;

    boolean DEFAULT_ignorable = true;

    String defaultKeySuffix() default DEFAULT_defaultKeySuffix;

    boolean shortCircuit() default DEFAULT_shortCircuit;
}
