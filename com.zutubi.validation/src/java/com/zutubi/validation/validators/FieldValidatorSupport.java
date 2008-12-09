package com.zutubi.validation.validators;

import com.zutubi.util.bean.BeanUtils;
import com.zutubi.util.bean.PropertyNotFoundException;
import com.zutubi.validation.FieldValidator;
import com.zutubi.validation.ValidationException;

/**
 * Helper base class for implementing field validators.  Includes support
 * for getting the field value and addition of field error messages.
 */
public abstract class FieldValidatorSupport extends ValidatorSupport implements FieldValidator
{
    private String fieldName;

    private String defaultKeySuffix = "invalid";

    protected FieldValidatorSupport()
    {
    }

    protected FieldValidatorSupport(String defaultKeySuffix)
    {
        this.defaultKeySuffix = defaultKeySuffix;
    }

    public String getFieldName()
    {
        return fieldName;
    }

    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public String getDefaultKeySuffix()
    {
        return defaultKeySuffix;
    }

    public void setDefaultKeySuffix(String defaultKeySuffix)
    {
        this.defaultKeySuffix = defaultKeySuffix;
    }

    public String getErrorMessage()
    {
        return getErrorMessage(defaultKeySuffix);
    }

    public String getErrorMessage(String keySuffix, Object... args)
    {
        Object[] actualArgs = new String[args.length + 1];
        actualArgs[0] = getFieldLabel();
        System.arraycopy(args, 0, actualArgs, 1, args.length);

        String error = validationContext.getText(fieldName + "." + keySuffix, actualArgs);
        if(error == null)
        {
            error = validationContext.getText("." + keySuffix, actualArgs);
            if(error == null)
            {
                error = actualArgs[0] + " is invalid";
            }
        }
        return error;
    }

    public void addError()
    {
        addError(defaultKeySuffix);
    }

    public void addError(String keySuffix, Object... args)
    {
        addErrorMessage(getErrorMessage(keySuffix, args));
    }

    public void addErrorMessage(String message)
    {
        validationContext.addFieldError(fieldName, message);
    }

    protected String getFieldLabel()
    {
        String label = validationContext.getText(fieldName + ".label");
        if(label == null)
        {
            label = fieldName;
        }
        return label;
    }

    public void validate(Object obj) throws ValidationException
    {
        validateField(getFieldValue(fieldName, obj));
    }

    protected Object getFieldValue(String fieldName, Object target) throws ValidationException
    {
        try
        {
            return BeanUtils.getProperty(fieldName, target);
        }
        catch (PropertyNotFoundException e)
        {
            throw new ValidationException("Field '" + fieldName + "' is not a property on object of type '" +
                    target.getClass().getName() + "'");
        }
        catch (Exception e)
        {
            throw new ValidationException("Failed to retrieve the field '" + fieldName +
                    "' from an object of type '" + target.getClass().getName() +
                    "'. Cause: " + e.getMessage(), e);
        }
    }

    protected abstract void validateField(Object value) throws ValidationException;
}
