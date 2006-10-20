package com.zutubi.pulse.scm;

import com.zutubi.pulse.config.Config;
import com.zutubi.pulse.scm.p4.P4WorkingCopy;
import com.zutubi.pulse.scm.svn.SvnWorkingCopy;
import com.zutubi.pulse.scm.cvs.CvsWorkingCopy;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.TreeMap;

/**
 */
public class WorkingCopyFactory
{
    private static Map<String, Constructor> typeMap = new TreeMap<String, Constructor>();

    static
    {
        try
        {
            registerType(SCMConfiguration.TYPE_SUBVERSION, SvnWorkingCopy.class);
            registerType(SCMConfiguration.TYPE_PERFORCE, P4WorkingCopy.class);
            registerType(SCMConfiguration.TYPE_CVS, CvsWorkingCopy.class);
        }
        catch (NoSuchMethodException e)
        {
            // Programmer error
        }
    }

    public static WorkingCopy create(String type, File base, Config config)
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
