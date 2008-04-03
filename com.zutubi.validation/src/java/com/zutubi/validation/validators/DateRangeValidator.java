package com.zutubi.validation.validators;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <class-comment/>
 */
public class DateRangeValidator extends AbstractRangeValidator<Date>
{
    private String dateFormat = "dd/MM/yyyy";

    private String min;
    private String max;

    public void setMax(String max)
    {
        this.max = max;
    }

    public String getMax()
    {
        return max;
    }

    public void setMin(String min)
    {
        this.min = min;
    }

    public String getMin()
    {
        return min;
    }

    protected Date getMaxComparatorValue()
    {
        if (max != null)
        {
            try
            {
                return toDate(max);
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }

    protected Date getMinComparatorValue()
    {
        if (min != null)
        {
            try
            {
                return toDate(min);
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }

    private Date toDate(String str) throws ParseException
    {
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        return format.parse(str);
    }
}
