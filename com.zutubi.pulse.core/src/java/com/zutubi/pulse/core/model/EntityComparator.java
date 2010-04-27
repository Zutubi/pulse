package com.zutubi.pulse.core.model;

import java.util.Comparator;

/**
 * A comparator implementation that can be used to compare entities by
 * their ids.
 */
public class EntityComparator<T extends Entity> implements Comparator<T>
{
    public int compare(T o1, T o2)
    {
        long diff = o2.getId() - o1.getId();
        if (diff > 0)
        {
            return -1;
        }
        if (diff < 0)
        {
            return 1;
        }
        return 0;
    }
}
