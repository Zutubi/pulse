package com.zutubi.pulse.core.config;

import com.zutubi.util.Sort;

import java.util.Comparator;

/**
 */
public class NamedConfigurationComparator implements Comparator<NamedConfiguration>
{
    private Sort.StringComparator c = new Sort.StringComparator();

    public int compare(NamedConfiguration s1, NamedConfiguration s2)
    {
        return c.compare(s1.getName(), s2.getName());
    }
}
