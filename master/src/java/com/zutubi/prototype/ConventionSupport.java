package com.zutubi.prototype;

import com.zutubi.prototype.type.Type;

import java.lang.reflect.Method;

/**
 *
 *
 */
public class ConventionSupport
{
    public static Class getWizard(Type type)
    {
        return loadClass(type, "Wizard");
    }

    public static Class getActions(Type type)
    {
        return loadClass(type, "Actions");
    }

    public static Class getFormatter(Type type)
    {
        return loadClass(type, "Formatter");
    }

    private static Class loadClass(Type type, String suffix)
    {
        // we need to search up the inheritence hierarchy.
        Class clazz = type.getClazz();
        while (clazz != Object.class)
        {
            try
            {
                String className = clazz.getCanonicalName() + suffix;
                return clazz.getClassLoader().loadClass(className);
            }
            catch (ClassNotFoundException e)
            {
                // noops.
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    public static Method getActionMethod(Class handlerClass, Type type, String action)
    {
        // again, we search up the inheritance hierarchy for the methods argument.

        Class clazz = type.getClazz();
        while (clazz != Object.class)
        {
            try
            {
                return handlerClass.getMethod("do" + action, clazz);
            }
            catch (NoSuchMethodException e)
            {
                // noop.
            }
            clazz = clazz.getSuperclass();
        }

        return null;
    }
}
