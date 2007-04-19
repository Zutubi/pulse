package com.zutubi.validation.annotations;

import com.zutubi.validation.validators.NumericValidator;

/**
 * <class-comment/>
 */
@Constraint(NumericValidator.class)
public @interface Max
{
    @ConstraintProperty("max")
    int value();
}
