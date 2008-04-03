package com.zutubi.validation.annotations;

/**
 * <class-comment/>
 */
@Constraint("com.zutubi.validation.validators.NumericValidator")
public @interface Max
{
    static final String DEFAULT_defaultKeySuffix = "";

    static final boolean DEFAULT_shortCircuit = true;

    String defaultKeySuffix() default DEFAULT_defaultKeySuffix;

    boolean shortCircuit() default DEFAULT_shortCircuit;

    @ConstraintProperty("max")
    int value();
}
