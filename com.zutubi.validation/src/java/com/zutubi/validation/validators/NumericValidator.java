package com.zutubi.validation.validators;

import com.zutubi.validation.ValidationException;

/**
 * Validates that numerical values fall within some range.
 */
public class NumericValidator extends FieldValidatorSupport
{
    public static final String MIN = ".min";

    public static final String MAX = ".max";

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

    public void validate(Object obj) throws ValidationException
    {
        Object value = getFieldValue(getFieldName(), obj);
        if (value instanceof Integer)
        {
            Integer integerValue = (Integer) value;
            if (integerValue != Integer.MIN_VALUE)
            {
                if (integerValue < min)
                {
                    addError(MIN);
                }

                if (max < integerValue)
                {
                    addError(MAX);
                }
            }
        }
        else if (value instanceof Long)
        {
            Long longValue = (Long) value;
            if (longValue != Long.MIN_VALUE)
            {
                if (longValue < min)
                {
                    addError(MIN);
                }

                if (max < longValue)
                {
                    addError(MAX);
                }
            }
        }
    }

    private void addError(String suffix)
    {
        setMessageKey(getFieldName() + suffix);
        addFieldError(getFieldName());
    }
}
