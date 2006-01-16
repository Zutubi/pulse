package com.cinnamonbob.core.util;

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
}
