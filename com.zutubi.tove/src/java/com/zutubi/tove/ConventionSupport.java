package com.zutubi.tove;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.Type;

/**
 * Helper methods for finding classes associated with configuration types,
 * for example for finding custom wizard classes.
 */
public class ConventionSupport
{
    public static final String I18N_KEY_SUFFIX_LABEL   = ".label";    

    public static Class getWizard(Type type)
    {
        return loadClass(type, "Wizard");
    }

    public static Class getActions(Class<? extends Configuration> clazz)
    {
        return loadClass(clazz, "Actions");
    }

    public static Class getLinks(Class<? extends Configuration> clazz)
    {
        return loadClass(clazz, "Links");
    }

    public static Class getCleanupTasks(Class<? extends Configuration> clazz)
    {
        return loadClass(clazz, "CleanupTasks");
    }

    public static Class getFormatter(Type type)
    {
        return loadClass(type, "Formatter");
    }

    public static Class getStateDisplay(Class<? extends Configuration> clazz)
    {
        return loadClass(clazz, "StateDisplay");
    }

    @SuppressWarnings({"unchecked"})
    public static Class<? extends Configuration> getCreator(Type type)
    {
        return loadClass(type, "Creator");
    }

    private static Class loadClass(Type type, String suffix)
    {
        // we need to search up the inheritence hierarchy.
        return loadClass(type.getClazz(), suffix);
    }

    private static Class loadClass(Class clazz, String suffix)
    {
        if(clazz.isArray() || clazz.isPrimitive())
        {
            return null;
        }
        
        while (clazz != null && clazz != Object.class)
        {
            try
            {
                String className = clazz.getName() + suffix;
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
}
