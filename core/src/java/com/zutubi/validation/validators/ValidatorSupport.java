package com.zutubi.validation.validators;

import com.zutubi.validation.Validator;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.ShortCircuitableValidator;
import com.zutubi.validation.bean.PropertyNotFoundException;
import com.zutubi.validation.bean.BeanUtils;

/**
 * <class-comment/>
 */
public abstract class ValidatorSupport implements Validator, ShortCircuitableValidator
{
    protected ValidationContext validationContext;

    private boolean shortCircuit = true;

    public ValidationContext getValidationContext()
    {
        return validationContext;
    }

    public void setValidationContext(ValidationContext validationContext)
    {
        this.validationContext = validationContext;
    }

    public void setShortCircuit(boolean b)
    {
        shortCircuit = b;
    }

    public boolean isShortCircuit()
    {
        return shortCircuit;
    }

}
