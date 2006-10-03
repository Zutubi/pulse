package com.zutubi.validation.validators;

import com.zutubi.validation.ValidationException;

/**
 * <class-comment/>
 */
public abstract class AbstractRangeValidator extends FieldValidatorSupport
{
    public void validate(Object object) throws ValidationException
    {
        Object obj = getFieldValue(getFieldName(), object);

        // if there is no value - don't do comparison. If a value is required, a required validator should be
        // added to the field
        if (obj == null)
        {
            return;
        }

        if (!(obj instanceof Comparable))
        {
            throw new ValidationException();
        }

        Comparable value = (Comparable) obj;

        // only check for a minimum value if the min parameter is set
        if ((getMinComparatorValue() != null) && (value.compareTo(getMinComparatorValue()) < 0))
        {
            validationContext.addFieldError(getFieldName(), object.toString());
        }

        // only check for a maximum value if the max parameter is set
        if ((getMaxComparatorValue() != null) && (value.compareTo(getMaxComparatorValue()) > 0))
        {
            validationContext.addFieldError(getFieldName(), object.toString());
        }
    }

    protected abstract Comparable getMaxComparatorValue();

    protected abstract Comparable getMinComparatorValue();
}
