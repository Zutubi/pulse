package com.cinnamonbob;

import com.cinnamonbob.core.model.Resource;

import java.util.Comparator;

/**
 * A comparator that orders resources lexically according to their names.
 */
public class ResourceNameComparator implements Comparator<Resource>
{
    public int compare(Resource r1, Resource r2)
    {
        return r1.getName().compareTo(r2.getName());
    }
}
