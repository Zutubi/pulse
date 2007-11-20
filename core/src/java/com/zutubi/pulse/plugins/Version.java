package com.zutubi.pulse.plugins;

import com.zutubi.util.TextUtils;

/**
 */
public class Version implements Comparable<Version>
{
    public static final Version NONE = new Version(0, 0, 0, null);
    public static final Version MAX = new Version(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, null);
    
    private int major;
    private int minor;
    private int service;
    private String qualifier;
    
    public Version(String version) throws IllegalArgumentException
    {
        String[] pieces = version.split("\\.", 4);
        if(pieces.length < 3)
        {
            throw new IllegalArgumentException("Version contains less than three segments");
        }

        try
        {
            major = Integer.parseInt(pieces[0]);
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Invalid major number '" + pieces[0] + "'");
        }

        try
        {
            minor = Integer.parseInt(pieces[1]);
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Invalid minor number '" + pieces[1] + "'");
        }

        try
        {
            service = Integer.parseInt(pieces[2]);
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Invalid service number '" + pieces[2] + "'");
        }

        if(pieces.length == 4)
        {
            qualifier = pieces[3];
        }
        else
        {
            qualifier = null;
        }
    }

    public Version(int major, int minor, int service, String qualifier)
    {
        this.major = major;
        this.minor = minor;
        this.service = service;
        this.qualifier = qualifier;
    }

    public int getMajor()
    {
        return major;
    }

    public int getMinor()
    {
        return minor;
    }

    public int getService()
    {
        return service;
    }

    public String getQualifier()
    {
        return qualifier;
    }

    public boolean equals(Object obj)
    {
        if(obj == null || !(obj instanceof Version))
        {
            return false;
        }

        Version other = (Version) obj;
        return compareTo(other) == 0;
    }

    public int hashCode()
    {
        return toString().hashCode();
    }

    public String toString()
    {
        return String.format("%d.%d.%d", major, minor, service) + (TextUtils.stringSet(qualifier) ? ("." + qualifier) : "");
    }

    public int compareTo(Version o)
    {
        int result = major - o.major;
        if(result == 0)
        {
            result = minor - o.minor;
            if(result == 0)
            {
                result = service - o.service;
                if(result == 0 && qualifier != null && o.qualifier != null)
                {
                    result = qualifier.compareTo(o.qualifier);
                }
            }
        }

        return result;
    }
}
