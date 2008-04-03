package com.zutubi.validation.validators;

import com.zutubi.validation.DelegatingValidationContext;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.ValidationManager;

import java.util.Collection;

/**
 * <class-comment/>
 */
public class DelegateValidator extends FieldValidatorSupport
{
    private ValidationManager validationManager;

    public void validateField(Object value) throws ValidationException
    {
        if (value != null)
        {
            // validate the value object.
            if (value instanceof Collection)
            {
                Collection coll = (Collection) value;
                Object[] array = coll.toArray();

                validateArrayElements(array);
            }
            else if (value instanceof Object[])
            {
                Object[] array = (Object[]) value;

                validateArrayElements(array);
            }
            else
            {
                validateObject(value, getFieldName());
            }
        }
    }

    private void validateArrayElements(Object[] array) throws ValidationException
    {
        String fieldName = getFieldName();
        for (int i = 0; i < array.length; i++)
        {
            validateObject(array[i], fieldName + "[" + i + "]");
        }
    }

    private void validateObject(Object value, String fieldName) throws ValidationException
    {
        validationManager.validate(value, new AppendingValidationContext(validationContext, value, fieldName));
    }

    public void setValidationManager(ValidationManager validationManager)
    {
        this.validationManager = validationManager;
    }

    private class AppendingValidationContext extends DelegatingValidationContext
    {
        private String fieldName;

        public AppendingValidationContext(ValidationContext parent, Object obj, String field)
        {
            this.validationAware = parent;
            this.textProvider = makeTextProvider(obj);
            this.fieldName = field;
        }

        public void addActionError(String error)
        {
            super.addFieldError(fieldName, error);
        }

        public void addFieldError(String field, String error)
        {
            super.addFieldError(getFullFieldName(field), error);
        }

        private String getFullFieldName(String field)
        {
            return this.fieldName + "." + field;
        }
    }
}
