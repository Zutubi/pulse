package com.zutubi.validation.annotations;

/**
 * <class-comment/>
 */
@Constraint("com.zutubi.validation.validators.NumericValidator")
public @interface Min
{
    static final String DEFAULT_defaultKeySuffix = "";

    static final boolean DEFAULT_shortCircuit = true;

    public abstract String defaultKeySuffix() default DEFAULT_defaultKeySuffix;

    public abstract boolean shortCircuit() default DEFAULT_shortCircuit;

    @ConstraintProperty("min") public abstract int value();
}