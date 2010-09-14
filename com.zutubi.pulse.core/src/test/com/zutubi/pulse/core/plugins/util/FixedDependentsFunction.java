package com.zutubi.pulse.core.plugins.util;

import com.zutubi.util.UnaryFunction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Fake dependent-generating function for testing.
 */
class FixedDependentsFunction implements UnaryFunction<String, Set<String>>
{
    private Map<String, Set<String>> dependencyMapping = new HashMap<String, Set<String>>();

    /**
     * Adds a string with a fixed set of dependents.
     *
     * @param s    the string to add
     * @param deps set of dependents for the string
     */
    public void add(String s, String... deps)
    {
        Set<String> set = get(s);
        set.addAll(java.util.Arrays.asList(deps));
    }

    public Set<String> process(String s)
    {
        return get(s);
    }

    private Set<String> get(String s)
    {
        Set<String> set = dependencyMapping.get(s);
        if (set == null)
        {
            set = new HashSet<String>();
            dependencyMapping.put(s, set);
        }
        return set;
    }
}
