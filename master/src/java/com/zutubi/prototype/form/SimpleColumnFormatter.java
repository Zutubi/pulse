package com.zutubi.prototype.form;

/**
 *
 *
 */
public class SimpleColumnFormatter implements ColumnFormatter
{
    public String format(Object obj)
    {
        if (obj != null)
        {
            return obj.toString();
        }
        return "";
    }
}
