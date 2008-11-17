package com.zutubi.pulse.core.model;

import com.zutubi.util.Sort;

import java.util.Comparator;

/**
 * Compares two named entities lexicographically by name using the default
 * string comparator.
 *
 * @see com.zutubi.util.Sort.StringComparator
 */
public class NamedEntityComparator implements Comparator<NamedEntity>
{
    private Sort.StringComparator c = new Sort.StringComparator();

    /**
     * Adheres to the standard {@link java.util.Comparator} contract by
     * ordering entities lexicographically by name.  Also handles null names by
     * effectively making null the first string lexicographically: i.e. less
     * than every other string bar itself.
     *
     * @param e1 the first entity, must not be null (although its name may be)
     * @param e2 the second entity, must not be null (although its name may be)
     * @return a negative integer, zero or positive integer if the first
     *         argument is less than, equal to or greater than the second
     *         argument as defined by the ordering described above
     */
    public int compare(NamedEntity e1, NamedEntity e2)
    {
        //noinspection StringEquality
        if (e1.getName() == e2.getName())
        {
            return 0;
        }

        if (e1.getName() == null)
        {
            return -1;
        }

        if (e2.getName() == null)
        {
            return 1;
        }

        return c.compare(e1.getName(), e2.getName());
    }
}
