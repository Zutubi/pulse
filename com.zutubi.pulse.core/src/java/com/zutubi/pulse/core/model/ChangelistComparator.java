package com.zutubi.pulse.core.model;

import java.util.Comparator;

/**
 */
public class ChangelistComparator implements Comparator<PersistentChangelist>
{
    public int compare(PersistentChangelist c1, PersistentChangelist c2)
    {
        // Compare the date.
        return -c1.getDate().compareTo(c2.getDate());
    }
}
