package com.zutubi.prototype;

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
