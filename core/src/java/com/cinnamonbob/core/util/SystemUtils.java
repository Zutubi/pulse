package com.zutubi.pulse.core.util;

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

    /**
     * Attempts to find a file with the given name in the PATH.
     *
     * @param name the name of the file to look for
     * @return the file in the path, or null if not found
     */
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

    public static String getLineSeparator()
    {
        if(isWindows())
        {
            return "\r\n";
        }
        else
        {
            return "\n";
        }
    }
}
