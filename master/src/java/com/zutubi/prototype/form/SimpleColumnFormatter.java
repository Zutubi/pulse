package com.zutubi.prototype.form;

/**
 *
 *
 */
public class SimpleColumnFormatter implements ColumnFormatter
{
    public String format(int index, Object obj)
    {
        return obj.toString();
    }
}
