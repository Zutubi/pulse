package com.zutubi.validation.validators;

/**
 * <class-comment/>
 */
public class IntegerRangeValidator extends AbstractRangeValidator
{
    Integer max = null;
    Integer min = null;

    public void setMax(Integer max)
    {
        this.max = max;
    }

    public Integer getMax()
    {
        return max;
    }

    public Comparable getMaxComparatorValue()
    {
        return max;
    }

    public void setMin(Integer min)
    {
        this.min = min;
    }

    public Integer getMin()
    {
        return min;
    }

    public Comparable getMinComparatorValue()
    {
        return min;
    }

}
