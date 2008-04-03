package com.zutubi.validation.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <class-comment/>
 */
@Constraint("com.zutubi.validation.validators.NumericValidator")
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Numeric
{
    static final String DEFAULT_defaultKeySuffix = "";

    static final boolean DEFAULT_shortCircuit = true;

    static final int DEFAULT_max = Integer.MAX_VALUE;

    static final int DEFAULT_min = Integer.MIN_VALUE;

    String defaultKeySuffix() default DEFAULT_defaultKeySuffix;

    boolean shortCircuit() default DEFAULT_shortCircuit;

    int max() default DEFAULT_max;

    int min() default DEFAULT_min;
}


