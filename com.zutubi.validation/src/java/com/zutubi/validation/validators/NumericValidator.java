package com.zutubi.validation.validators;

import com.zutubi.validation.ValidationException;

/**
 * Validates that numerical values fall within some range.
 */
public class NumericValidator extends FieldValidatorSupport
{
    public static final String MIN = "min";

    public static final String MAX = "max";

    private long max = Long.MAX_VALUE;
    
    private long min = Long.MIN_VALUE;

    public void setMax(long max)
    {
        this.max = max;
    }

    public void setMin(long min)
    {
        this.min = min;
    }

    public long getMax()
    {
        return max;
    }

    public long getMin()
    {
        return min;
    }

    public void validateField(Object value) throws ValidationException
    {
        if (value instanceof String)
        {
            try
            {
                value = Long.valueOf((String)value);
            }
            catch (NumberFormatException e)
            {
                addError();
                return;
            }
        }

        if (value instanceof Number)
        {
            Number number = (Number) value;
            if (isSet(number))
            {
                if (number.longValue() < min)
                {
                    addError(MIN);
                }
                if (max < number.longValue())
                {
                    addError(MAX);
                }
            }
        }
    }

    private boolean isSet(Number number)
    {
        if (number != null)
        {
            if (number instanceof Long)
            {
                return number.longValue() != Long.MIN_VALUE;
            }
            else if (number instanceof Integer)
            {
                return number.intValue() != Integer.MIN_VALUE;
            }
            else if (number instanceof Short)
            {
                return number.intValue() != Short.MIN_VALUE;
            }
            else if (number instanceof Byte)
            {
                return number.intValue() != Byte.MIN_VALUE;
            }
        }
        return false;
    }
}
