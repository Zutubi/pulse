package com.zutubi.pulse.validation.validators;

import com.zutubi.validation.validators.FieldValidatorSupport;
import com.zutubi.validation.validators.IgnoreDependentsFieldValidator;
import com.zutubi.validation.ValidationException;

/**
 * A validator that ignores dependent fields when a controlling checkbox is
 * in a certain state.  Delegates to a generic field ignoring validator.
 */
public class ControllingCheckboxValidator extends FieldValidatorSupport
{
    private boolean invert;
    private String[] dependentFields;

    public void setInvert(boolean invert)
    {
        this.invert = invert;
    }

    public void setDependentFields(String[] dependentFields)
    {
        this.dependentFields = dependentFields;
    }

    public void validate(Object object) throws ValidationException
    {
        IgnoreDependentsFieldValidator delegate = new IgnoreDependentsFieldValidator();
        delegate.setFieldName(getFieldName());
        delegate.setNonIgnoreValue(Boolean.toString(!invert));
        delegate.setDependentFields(dependentFields);
        delegate.setValidationContext(getValidationContext());
        delegate.validate(object);
    }
}
