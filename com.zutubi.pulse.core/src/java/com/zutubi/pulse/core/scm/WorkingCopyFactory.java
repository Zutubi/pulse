package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.api.PersonalBuildException;
import com.zutubi.pulse.core.scm.api.WorkingCopy;
import com.zutubi.pulse.core.util.config.Config;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.TreeMap;

/**
 */
public class WorkingCopyFactory
{
    private static Map<String, Constructor> typeMap = new TreeMap<String, Constructor>();

    public static WorkingCopy create(String type, File base, Config config) throws PersonalBuildException
    {
        Constructor constructor = typeMap.get(type);
        if(constructor != null)
        {
            try
            {
                return (WorkingCopy) constructor.newInstance(base, config);
            }
            catch (Exception e)
            {
                if (e instanceof PersonalBuildException)
                {
                    throw (PersonalBuildException)e;
                }
                if (e.getCause() instanceof PersonalBuildException)
                {
                    throw (PersonalBuildException)e.getCause();
                }
                return null;
            }
        }

        return null;
    }

    public static void registerType(String type, Class clazz) throws IllegalArgumentException, NoSuchMethodException
    {
        if(!WorkingCopy.class.isAssignableFrom(clazz))
        {
            throw new IllegalArgumentException("Class '" + clazz.getName() + "' does not implement WorkingCopy");
        }

        Constructor constructor = clazz.getConstructor(File.class, Config.class);
        typeMap.put(type, constructor);
    }
}
