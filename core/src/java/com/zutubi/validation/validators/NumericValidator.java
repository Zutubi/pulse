package com.zutubi.validation.validators;

import com.zutubi.validation.FieldValidator;

/**
 * <class-comment/>
 */
public class NumericValidator extends FieldValidatorSupport
{
    private int max;
    private int min;

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

    public void validate(Object obj)
    {

    }
}
