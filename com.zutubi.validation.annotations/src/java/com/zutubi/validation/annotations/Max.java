package com.zutubi.validation.annotations;

/**
 * This annotation marks the field as numeric, constrained by the configured
 * maximum value.
 */
@Constraint("com.zutubi.validation.validators.NumericValidator")
public @interface Max
{
    static final String DEFAULT_defaultKeySuffix = "";

    static final boolean DEFAULT_shortCircuit = true;

    String defaultKeySuffix() default DEFAULT_defaultKeySuffix;

    boolean shortCircuit() default DEFAULT_shortCircuit;

    /**
     * The maximum allowed value for the annotated field.
     *
     * @return the maximum value.
     */
    @ConstraintProperty("max") int value();
}
