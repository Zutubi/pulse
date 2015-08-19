package com.zutubi.tove;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.Type;
import com.zutubi.util.reflection.ReflectionUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Helper methods for finding classes associated with configuration types,
 * for example for finding custom wizard classes.
 */
public class ConventionSupport
{
    public static final String I18N_KEY_SUFFIX_LABEL   = ".label";

    @SuppressWarnings({"unchecked"})
    public static Class<? extends Configuration> getCheckHandler(Type type)
    {
        return loadClass(type, "CheckHandler");
    }

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

    public static Class<?> getFormatter(Type type)
    {
        return loadClass(type, "Formatter");
    }

    public static Class getStateDisplay(Class<? extends Configuration> clazz)
    {
        return loadClass(clazz, "StateDisplay");
    }

    public static Class getClassifier(Class<? extends Configuration> clazz)
    {
        return loadClass(clazz, "Classifier");
    }

    public static Class getExamples(Class<? extends Configuration> clazz)
    {
        return loadClass(clazz, "Examples");
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
        Class searchClass = clazz;
        if (searchClass.isArray() || searchClass.isPrimitive())
        {
            return null;
        }
        
        while (searchClass != null && searchClass != Object.class)
        {
            try
            {
                String className = searchClass.getName() + suffix;
                return searchClass.getClassLoader().loadClass(className);
            }
            catch (ClassNotFoundException e)
            {
                // noops.
            }
            searchClass = searchClass.getSuperclass();
        }

        // Interfaces are done separately, as there is no longer a total
        // ordering.  Because of this we actually verify that we only find one
        // matching *Actions interface (we don't want silent non-determinism).
        Set<Class> implementedInterfaces = ReflectionUtils.getImplementedInterfaces(clazz, Object.class, true);
        Set<Class> foundInterfaces = new HashSet<Class>();
        for (Class iface: implementedInterfaces)
        {
            if (iface != Configuration.class && Configuration.class.isAssignableFrom(iface))
            {
                try
                {
                    String interfaceName = iface.getName() + suffix;
                    foundInterfaces.add(iface.getClassLoader().loadClass(interfaceName));
                }
                catch (ClassNotFoundException e)
                {
                    // noops.
                }
            }
        }

        if (foundInterfaces.isEmpty())
        {
            return null;
        }
        else if (foundInterfaces.size() == 1)
        {
            return foundInterfaces.iterator().next();
        }
        else
        {
            String message = "Unable to resolve " + suffix + " type for class '" + clazz.getName() + "': multiple candidate interfaces found:";
            for (Class iface: foundInterfaces)
            {
                message += " '" + iface.getName() + "'";
            }
            
            throw new RuntimeException(message);
        }
    }
}
