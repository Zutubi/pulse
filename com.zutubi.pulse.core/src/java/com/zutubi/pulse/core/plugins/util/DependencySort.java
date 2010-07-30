package com.zutubi.pulse.core.plugins.util;

import com.zutubi.util.UnaryFunction;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Utility class to help sorting in the plugin-related classes.
 */
public class DependencySort
{
    /**
     * Sorts the given collection of plugins such that no plugin appears after
     * a dependent.  This allows the plugins to be "added" in the order they
     * are found in the returned list.
     *
     * @param plugins            plugins to sort
     * @param directDependentsFn a function that maps from a plugin to the set
     *                           of its direct dependents
     * @param <T> the type of plugin we are dealing with
     * @return the sorted collection of plugins
     */
    public static <T> List<T> sort(List<T> plugins, UnaryFunction<T, Set<T>> directDependentsFn)
    {
        // A normal sort will not work as there is no ordering relationship
        // between plugins that have no dependency relationship.
        List<T> sorted = new LinkedList<T>();
        for (T plugin: plugins)
        {
            // Insert it as late as we can in sorted without inserting after
            // a transitive dependent.  If a dependent comes first, we are
            // sure to insert before it.  If it comes after, it will end up
            // after by virtue of being inserted as late as possible.
            int i;
            Set<T> dependents = new TransitiveFunction<T>(directDependentsFn).process(plugin);
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
