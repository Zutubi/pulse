package com.zutubi.pulse.core.model;

import java.util.Comparator;

/**
 * Orders changelists so the most recent comes first.
 */
public class ChangelistComparator implements Comparator<PersistentChangelist>
{
    public int compare(PersistentChangelist c1, PersistentChangelist c2)
    {
        int comp = c2.getDate().compareTo(c1.getDate());
        if (comp == 0)
        {
            // If dates match, compare by id (inverse).
            long id1 = c1.getId();
            long id2 = c2.getId();
            if (id2 < id1)
            {
                comp = -1;
            }
            else if (id2 > id1)
            {
                comp = 1;
            }
        }
        return comp;
    }
}
