package com.zutubi.pulse.master.agent;

/**
 * Utility class to format host locations.
 */
public class HostLocationFormatter
{
    public static final String LOCATION_MASTER = "[master]";

    /**
     * Formats a location into a single string which is human-readable and
     * unique for different locations.
     *
     * @param location the location to format
     * @return the formatted location
     */
    public static String format(HostLocation location)
    {
        if (location.isRemote())
        {
            return (location.getHostName() == null ? "" : location.getHostName()) + ":" + location.getPort();
        }
        else
        {
            return LOCATION_MASTER;
        }
    }
}
