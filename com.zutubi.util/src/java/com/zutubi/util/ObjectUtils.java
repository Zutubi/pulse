package com.zutubi.util;

import java.util.List;
import java.util.LinkedList;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

    public static void setProperty(String name, Object value, Object target) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        Method m = target.getClass().getDeclaredMethod(name, value.getClass());
        m.setAccessible(true);
        m.invoke(target, value);
    }

    public static Object getField(String name, Object target) throws NoSuchFieldException, IllegalAccessException
    {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(target);
    }
}
