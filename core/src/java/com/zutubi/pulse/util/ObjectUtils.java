package com.zutubi.pulse.util;

/**
 * <class-comment/>
 */
public class ObjectUtils
{
    public static boolean equals(Object a, Object b)
    {
        if (a == null)
        {
            return b == null;
        }
        if (b == null)
        {
            return false;
        }
        return a.equals(b);
    }
}
