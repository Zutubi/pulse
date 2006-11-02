package com.zutubi.pulse.core.model;

import java.util.Comparator;

/**
 */
public class ChangelistComparator implements Comparator<Changelist>
{
    public int compare(Changelist c1, Changelist c2)
    {
        // Compare the date.
        return -c1.getDate().compareTo(c2.getDate());
    }
}
