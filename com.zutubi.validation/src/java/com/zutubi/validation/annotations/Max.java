package com.zutubi.validation.annotations;

/**
 * <class-comment/>
 */
@Constraint("com.zutubi.validation.validators.NumericValidator")
public @interface Max
{
    @ConstraintProperty("max")
    int value();
}
