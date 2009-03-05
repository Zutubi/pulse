package com.zutubi.validation.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks the field as numeric, constrained by the configured
 * minimum value.
 */
@Constraint("com.zutubi.validation.validators.NumericValidator")
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Min
{
    static final String DEFAULT_defaultKeySuffix = "";

    static final boolean DEFAULT_shortCircuit = true;

    public abstract String defaultKeySuffix() default DEFAULT_defaultKeySuffix;

    public abstract boolean shortCircuit() default DEFAULT_shortCircuit;

    /**
     * The minimum allowed value for the annotated field.
     *
     * @return the minimum value.
     */
    @ConstraintProperty("min") public int value();
}