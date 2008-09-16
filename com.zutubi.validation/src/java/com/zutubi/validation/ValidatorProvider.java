package com.zutubi.validation;

import java.util.List;

/**
 * The validation provider interface defines a validator factory.
 */
public interface ValidatorProvider
{
    /**
     * Get the list of validators applicable to the specified object being
     * validated within the given validation context.
     *
     * @param obj to be validated.
     * @param context for this validation
     *
     * @return list of validators to be used to validate the object.
     */
    List<Validator> getValidators(Object obj, ValidationContext context);
}
