package com.zutubi.tove.validation;

import com.zutubi.validation.ValidationException;
import com.zutubi.validation.validators.FieldValidatorSupport;
import com.zutubi.validation.validators.IgnoreDependentsFieldValidator;

/**
 * A validator that ignores dependent fields when a controlling select is
 * in certain states.  Delegates to a generic field ignoring validator.
 */
public class ControllingSelectValidator extends FieldValidatorSupport
{
    private String[] enableSet;
    private String[] dependentFields;

    public void setEnableSet(String[] enableSet)
    {
        this.enableSet = enableSet;
    }

    public void setDependentFields(String[] dependentFields)
    {
        this.dependentFields = dependentFields;
    }

    public void validateField(Object object) throws ValidationException
    {
        IgnoreDependentsFieldValidator delegate = new IgnoreDependentsFieldValidator();
        delegate.setFieldName(getFieldName());
        delegate.setNonIgnoreValues(enableSet);
        delegate.setDependentFields(dependentFields);
        delegate.setValidationContext(getValidationContext());
        delegate.validateField(object);
    }
}
