package com.zutubi.pulse.plugins;

import com.zutubi.pulse.plugins.Version;

/**
 * Stores a range of versions, used in expression of dependencies.
 */
public class VersionRange
{
    private Version min;
    private boolean includeMin;
    private Version max;
    private boolean includeMax;

    public VersionRange(Version min, boolean includeMin, Version max, boolean includeMax)
    {
        this.min = min;
        this.includeMin = includeMin;
        this.max = max;
        this.includeMax = includeMax;
    }

    public Version getMin()
    {
        return min;
    }

    public boolean isIncludeMin()
    {
        return includeMin;
    }

    public Version getMax()
    {
        return max;
    }

    public boolean isIncludeMax()
    {
        return includeMax;
    }

    public String toString()
    {
        if (min == null)
        {
            return Version.NONE.toString();
        }

        if (Version.MAX.equals(max))
        {
            return min.toString();
        }

        return (includeMin ? "[" : "(") + min + ", " + max + (includeMax ? "]" : ")");
    }
}
