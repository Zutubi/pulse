package com.zutubi.validation.validators;

import com.zutubi.validation.Validator;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.ShortCircuitableValidator;

/**
 * The validator support class implements the boiler plate methods from the Validator and
 * ShortCircuitValidator interfaces, allowing validator implementations to focus on the
 * details of the validation.
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
