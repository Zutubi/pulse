package com.zutubi.validation.validators;

import com.zutubi.validation.ValidationException;

/**
 * <class-comment/>
 */
public abstract class AbstractRangeValidator<T extends Comparable> extends FieldValidatorSupport
{
    @SuppressWarnings({"unchecked"})
    public void validateField(Object obj) throws ValidationException
    {
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
            addError(".min");
        }

        // only check for a maximum value if the max parameter is set
        if ((getMaxComparatorValue() != null) && (value.compareTo(getMaxComparatorValue()) > 0))
        {
            addError(".max");
        }
    }

    protected abstract T getMaxComparatorValue();
    protected abstract T getMinComparatorValue();
}
