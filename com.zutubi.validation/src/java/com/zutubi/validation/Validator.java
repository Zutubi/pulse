package com.zutubi.validation;

/**
 * The base interface used by all objects that validate something.
 */
public interface Validator
{
    /**
     * Getter for the wired validation context.
     *
     * @return wired validation context.
     */
    ValidationContext getValidationContext();

    /**
     * Allow the validation context to be wired into the validator.
     *
     * @param ctx the validation context
     */
    void setValidationContext(ValidationContext ctx);

    /**
     * Validate the specified object, recording the results of the validation
     * within the wired validation context.
     *
     * @param obj to be validated
     *
     * @throws ValidationException if a problem occurs during the validation that
     * prevents the validation from completing successfully.
     */
    void validate(Object obj) throws ValidationException;

}
