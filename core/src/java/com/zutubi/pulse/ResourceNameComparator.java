package com.zutubi.pulse;

import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.util.Sort;

import java.util.Comparator;

/**
 * A comparator that orders resources lexically according to their names.
 */
public class ResourceNameComparator implements Comparator<Resource>
{
    private Sort.StringComparator sc = new Sort.StringComparator();

    public int compare(Resource r1, Resource r2)
    {
        return sc.compare(r1.getName(), r2.getName());
    }
}
