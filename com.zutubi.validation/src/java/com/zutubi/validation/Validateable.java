package com.zutubi.validation;

/**
 * Base interface implemented by an object that can be validated.
 */
public interface Validateable
{
    /**
     * The implementation of this method carries out the validation processing.
     *
     * @param context in which the validation occurs.  
     */
    void validate(ValidationContext context);
}
