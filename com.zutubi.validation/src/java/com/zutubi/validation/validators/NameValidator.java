package com.zutubi.validation.validators;

/**
 * <class-comment/>
 */
public class NameValidator extends RegexValidator
{
    public NameValidator()
    {
        setPattern("[a-zA-Z0-9]+|[-a-zA-Z0-9_.]+[-a-zA-Z0-9_. ]*[-a-zA-Z0-9_.]+");
    }
}
