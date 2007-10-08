package com.zutubi.pulse.acceptance;

/**
 */
public class IDs
{
    public static String buildNumberCell(String project, long number)
    {
        return project + ".build." + Long.toString(number) + ".status";
    }
}
