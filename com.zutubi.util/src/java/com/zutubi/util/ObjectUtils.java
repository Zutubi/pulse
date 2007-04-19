package com.zutubi.util;

import java.util.List;
import java.util.LinkedList;

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

    /**
     * A simple utility for creating a list that contain the parameters.
     */
    public static <T> List<T> asList(T... o)
    {
        List<T> l = new LinkedList<T>();
        for (T obj : o)
        {
            l.add(obj);
        }
        return l;
    }
}
