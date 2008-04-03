package com.zutubi.validation.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <class-comment/>
 */
@Constraint("com.zutubi.validation.validators.RequiredValidator")
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Required
{
    static final String DEFAULT_defaultKeySuffix = "";

    static final boolean DEFAULT_shortCircuit = true;

    static final boolean DEFAULT_ignorable = true;

    String defaultKeySuffix() default DEFAULT_defaultKeySuffix;

    boolean shortCircuit() default DEFAULT_shortCircuit;

    boolean ignorable() default DEFAULT_ignorable;
}
