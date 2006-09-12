package com.zutubi.validation.validators;

import com.zutubi.validation.Validator;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.Validateable;

/**
 * <class-comment/>
 */
public class ValidateableValidator extends ValidatorSupport
{
    public void validate(Object obj)
    {
        if (Validateable.class.isAssignableFrom(obj.getClass()))
        {
            ((Validateable)obj).validate();
        }
    }
}
