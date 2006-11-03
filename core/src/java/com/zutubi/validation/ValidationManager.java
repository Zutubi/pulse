package com.zutubi.validation;

/**
 * <class-comment/>
 */
public interface ValidationManager
{
    void validate(Object o, ValidationContext context) throws ValidationException;
}
