package com.zutubi.pulse.form.validator.validators;

import com.zutubi.pulse.form.bean.BeanSupport;
import com.zutubi.pulse.form.bean.PropertyNotFoundException;
import com.zutubi.pulse.form.validator.ValidationException;
import com.zutubi.pulse.form.validator.Validator;
import com.zutubi.pulse.form.validator.ValidatorContext;

/**
 * <class-comment/>
 */
public abstract class ValidatorSupport implements Validator
{
    protected ValidatorContext context;

    public void setValidatorContext(ValidatorContext context)
    {
        this.context = context;
    }

    protected void addFieldError(String name, String error)
    {
        context.addFieldError(name, error);
    }

    protected void addActionError(String error)
    {
        context.addActionError(error);
    }

    protected boolean hasErrors()
    {
        return context.hasErrors();
    }

    protected Object getFieldValue(String name, Object target) throws ValidationException
    {
        try
        {
            return BeanSupport.getProperty(name, target);
        }
        catch (PropertyNotFoundException e)
        {
            throw new ValidationException("Field '" + name + "' is not a property on object of type '" +
                    target.getClass().getName() + "'");
        }
        catch (Exception e)
        {
            throw new ValidationException("Failed to retrieve the field '" + name +
                    "' from an object of type '" + target.getClass().getName() +
                    "'. Cause: " + e.getMessage(), e);
        }
    }
}
