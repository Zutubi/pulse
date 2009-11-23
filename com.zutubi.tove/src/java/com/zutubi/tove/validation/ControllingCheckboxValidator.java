package com.zutubi.tove.validation;

import com.zutubi.validation.ValidationException;
import com.zutubi.validation.validators.FieldValidatorSupport;
import com.zutubi.validation.validators.IgnoreDependentsFieldValidator;

/**
 * A validator that ignores dependent fields when a controlling checkbox is
 * in a certain state.  Delegates to a generic field ignoring validator.
 */
public class ControllingCheckboxValidator extends FieldValidatorSupport
{
    private String[] checkedFields;
    private String[] uncheckedFields;

    public void setCheckedFields(String[] checkedFields)
    {
        this.checkedFields = checkedFields;
    }

    public void setUncheckedFields(String[] uncheckedFields)
    {
        this.uncheckedFields = uncheckedFields;
    }

    public void validateField(Object object) throws ValidationException
    {
        if (checkedFields.length == 0 && uncheckedFields.length == 0)
        {
            IgnoreDependentsFieldValidator delegate = createDelegate();
            delegate.setNonIgnoreValues(Boolean.toString(true));
            delegate.validateField(object);
        }
        else
        {
            if (checkedFields.length > 0)
            {
                IgnoreDependentsFieldValidator checkedDelegate = createDelegate();
                checkedDelegate.setNonIgnoreValues(Boolean.toString(true));
                checkedDelegate.setDependentFields(checkedFields);
                checkedDelegate.validateField(object);
            }

            if (uncheckedFields.length > 0)
            {
                IgnoreDependentsFieldValidator uncheckedDelegate = createDelegate();
                uncheckedDelegate.setNonIgnoreValues(Boolean.toString(false));
                uncheckedDelegate.setDependentFields(uncheckedFields);
                uncheckedDelegate.validateField(object);
            }
        }
    }

    private IgnoreDependentsFieldValidator createDelegate()
    {
        IgnoreDependentsFieldValidator delegate = new IgnoreDependentsFieldValidator();
        delegate.setFieldName(getFieldName());
        delegate.setValidationContext(validationContext);
        return delegate;
    }
}
