package com.zutubi.validation.validators;

import com.zutubi.validation.FieldValidator;
import com.zutubi.validation.ValidationException;

/**
 * <class-comment/>
 */
public class NumericValidator extends FieldValidatorSupport
{
    public static final String MIN = ".min";

    public static final String MAX = ".max";

    private int max = Integer.MAX_VALUE;
    
    private int min = Integer.MIN_VALUE;

    public void setMax(int max)
    {
        this.max = max;
    }

    public void setMin(int min)
    {
        this.min = min;
    }

    public int getMax()
    {
        return max;
    }

    public int getMin()
    {
        return min;
    }

    public void validate(Object obj) throws ValidationException
    {
        Object value = getFieldValue(getFieldName(), obj);
        if (value instanceof Integer)
        {
            Integer integerValue = (Integer) value;
            if (integerValue < min)
            {
                validationContext.addFieldError(getFieldName(), getFieldName() + MIN);
            }

            if (max < integerValue)
            {
                validationContext.addFieldError(getFieldName(), getFieldName() + MAX);
            }
        }
    }
}
