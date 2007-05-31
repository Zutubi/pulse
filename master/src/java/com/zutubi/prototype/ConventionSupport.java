package com.zutubi.prototype;

import com.zutubi.prototype.type.Type;

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
        try
        {
            Class clazz = type.getClazz();
            String wizardClassName = clazz.getCanonicalName() + suffix;
            return clazz.getClassLoader().loadClass(wizardClassName);
        }
        catch (Exception e)
        {
            // noop.
        }
        return null;
    }
}
