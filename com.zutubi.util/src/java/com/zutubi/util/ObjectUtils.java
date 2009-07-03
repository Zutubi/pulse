package com.zutubi.util;

/**
 * Generic utilities for dealng with objects.
 */
public class ObjectUtils
{
    /**
     * Null-safe equals.  Defers to a.equals unless one of a or b is null in
     * which case it checks if they are both null.
     *
     * @param a first object
     * @param b second object
     * @return true if both objects are null, or a.equals(b)
     */
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
