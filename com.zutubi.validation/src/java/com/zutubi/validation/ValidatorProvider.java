package com.zutubi.validation;

import java.util.List;

/**
 * The validation provider interface defines a validator factory.
 */
public interface ValidatorProvider
{
    /**
     * Get the list of validators applicable to the specified class within the given context.
     *
     * @param clazz type of objects to validate
     *
     * @return list of validators to be used to validate the object.
     */
    List<Validator> getValidators(Class clazz);
}
