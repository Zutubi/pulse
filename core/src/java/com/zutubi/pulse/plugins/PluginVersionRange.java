package com.zutubi.pulse.plugins;

/**
 * Stores a range of versions, used in expression of dependencies.
 */
public class PluginVersionRange
{
    private PluginVersion min;
    private boolean includeMin;
    private PluginVersion max;
    private boolean includeMax;

    public PluginVersionRange(PluginVersion min, boolean includeMin, PluginVersion max, boolean includeMax)
    {
        this.min = min;
        this.includeMin = includeMin;
        this.max = max;
        this.includeMax = includeMax;
    }

    public PluginVersion getMin()
    {
        return min;
    }

    public boolean isIncludeMin()
    {
        return includeMin;
    }

    public PluginVersion getMax()
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
            return PluginVersion.NONE.toString();
        }

        if (PluginVersion.MAX.equals(max))
        {
            return min.toString();
        }

        return (includeMin ? "[" : "(") + min + ", " + max + (includeMax ? "]" : ")");
    }
}
