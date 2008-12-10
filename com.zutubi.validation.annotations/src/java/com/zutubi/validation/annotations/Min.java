package com.zutubi.validation.annotations;

/**
 * This annotation marks the field as numeric, constrained by the configured
 * minimum value.
 */
@Constraint("com.zutubi.validation.validators.NumericValidator")
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
    @ConstraintProperty("min") public abstract int value();
}