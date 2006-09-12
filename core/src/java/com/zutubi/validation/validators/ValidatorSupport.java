package com.zutubi.validation.validators;

import com.zutubi.validation.Validator;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.bean.PropertyNotFoundException;
import com.zutubi.validation.bean.BeanUtils;

/**
 * <class-comment/>
 */
public abstract class ValidatorSupport implements Validator
{
    protected ValidationContext validationContext;

    public ValidationContext getValidationContext()
    {
        return validationContext;
    }

    public void setValidationContext(ValidationContext validationContext)
    {
        this.validationContext = validationContext;
    }

    protected Object getFieldValue(String name, Object target) throws ValidationException
    {
        try
        {
            return BeanUtils.getProperty(name, target);
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
