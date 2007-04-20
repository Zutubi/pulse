package com.zutubi.prototype;

/**
 *
 *
 */
public class SimpleColumnFormatter implements Formatter<Object>
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
