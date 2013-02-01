package com.zutubi.pulse.core.plugins.util;

import com.google.common.base.Function;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Utility class to help sorting by dependency relationships.
 */
public class DependencySort
{
    /**
     * Sorts the given collection of items such that no item appears after a
     * dependent as defined by the given function.  Some items may have no
     * dependency relationship, in which case their returned relative order is
     * undefined.  The dependency graph must not contain any cycles.
     *
     * @param items              items to sort
     * @param directDependentsFn a function that maps from an item to the set
     *                           of its direct dependents (i.e. the set of all
     *                           items that depend upon this item)
     * @param <T> the type of item we are dealing with
     * @return the sorted collection of items
     */
    public static <T> List<T> sort(List<T> items, Function<T, Set<T>> directDependentsFn)
    {
        // A normal sort will not work as there is no ordering relationship
        // between plugins that have no dependency relationship.
        List<T> sorted = new LinkedList<T>();
        for (T plugin: items)
        {
            // Insert it as late as we can in sorted without inserting after
            // a transitive dependent.  If a dependent comes first, we are
            // sure to insert before it.  If it comes after, it will end up
            // after by virtue of being inserted as late as possible.
            int i;
            Set<T> dependents = new TransitiveFunction<T>(directDependentsFn).apply(plugin);
            for (i = 0; i < sorted.size(); i++)
            {
                if (dependents.contains(sorted.get(i)))
                {
                    break;
                }
            }
            sorted.add(i, plugin);
        }

        return sorted;
    }
}
