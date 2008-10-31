package com.zutubi.tove.config;

import com.zutubi.util.Sort;
import com.zutubi.tove.config.api.NamedConfiguration;

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
