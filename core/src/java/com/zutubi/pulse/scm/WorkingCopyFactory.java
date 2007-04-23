package com.zutubi.pulse.scm;

import com.zutubi.pulse.config.Config;
import com.zutubi.pulse.personal.PersonalBuildException;
import com.zutubi.pulse.scm.cvs.CvsWorkingCopy;
import com.zutubi.pulse.scm.p4.PerforceWorkingCopy;
import com.zutubi.pulse.scm.svn.SvnWorkingCopy;

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
            registerType(ScmConfiguration.TYPE_SUBVERSION, SvnWorkingCopy.class);
            registerType(ScmConfiguration.TYPE_PERFORCE, PerforceWorkingCopy.class);
            registerType(ScmConfiguration.TYPE_CVS, CvsWorkingCopy.class);
        }
        catch (NoSuchMethodException e)
        {
            // Programmer error
        }
    }

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
