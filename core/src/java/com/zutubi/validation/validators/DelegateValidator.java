package com.zutubi.validation.validators;

import com.zutubi.validation.*;

import java.util.Collection;

/**
 * <class-comment/>
 */
public class DelegateValidator extends FieldValidatorSupport
{
    private ValidationManager validationManager;

    public void validate(Object obj) throws ValidationException
    {
        String fieldName = getFieldName();
        Object value = getFieldValue(fieldName, obj);
        if (value != null)
        {
            // validate the value object.
            if (value instanceof Collection)
            {
                Collection coll = (Collection) value;
                Object[] array = coll.toArray();

                validateArrayElements(array, fieldName);
            }
            else if (value instanceof Object[])
            {
                Object[] array = (Object[]) value;

                validateArrayElements(array, fieldName);
            }
            else
            {
                validateObject(value, fieldName);
            }
        }
    }

    private void validateArrayElements(Object[] array, String fieldName) throws ValidationException
    {
        for (int i = 0; i < array.length; i++)
        {
            validateObject(array[i], fieldName + "[" + i + "]");
        }
    }

    private void validateObject(Object value, String fieldName) throws ValidationException
    {
        validationManager.validate(value, new AppendingValidationContext(validationContext, value, fieldName));
    }

    /**
     * Required resource.
     *
     * @param validationManager
     */
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
            this.textProvider = makeTextPovider(obj);
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

        public String getFullFieldName(String field)
        {
            return this.fieldName + "." + field;
        }
    }
}
