package com.zutubi.validation.validators;

import com.zutubi.validation.ValidationException;

/**
 * <class-comment/>
 */
public class NameValidator extends RegexValidator
{
    public NameValidator()
    {
        setPattern("[a-zA-Z0-9][-a-zA-Z0-9_. ]*");
    }
}
