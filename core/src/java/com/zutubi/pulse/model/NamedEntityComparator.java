package com.zutubi.pulse.model;

import com.zutubi.pulse.util.Sort;

import java.util.Comparator;

/**
 */
public class NamedEntityComparator implements Comparator<NamedEntity>
{
    private Sort.StringComparator c = new Sort.StringComparator();

    public int compare(NamedEntity s1, NamedEntity s2)
    {
        return c.compare(s1.getName(), s2.getName());
    }
}
