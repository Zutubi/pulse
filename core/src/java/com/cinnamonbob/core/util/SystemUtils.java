package com.cinnamonbob.core.util;

import java.io.File;

/**
 */
public class SystemUtils
{
    public static String osName()
    {
        return System.getProperty("os.name");
    }

    public static boolean isLinux()
    {
        return osName().equals("Linux");
    }

    public static boolean isWindows()
    {
        return osName().toLowerCase().contains("win");
    }

    public static File findInPath(String name)
    {
        String path = System.getenv("PATH");
        if (path != null)
        {
            String[] paths = path.split(File.pathSeparator);
            for (String dir : paths)
            {
                File test = new File(dir, name);
                if (test.isFile())
                {
                    return test;
                }
            }
        }

        return null;
    }
}
