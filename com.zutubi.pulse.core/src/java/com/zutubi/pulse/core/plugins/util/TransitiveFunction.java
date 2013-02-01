package com.zutubi.pulse.core.plugins.util;

import com.google.common.base.Function;

import java.util.HashSet;
import java.util.Set;

/**
 * A transitive version of a mapping from an instance to a set.  Given a
 * function representing the mapping, this function will act as the transitive
 * version of that mapping.  That is, the set produced by this function
 * includes all instances reachable by recursive application of the original
 * function.
 */
public class TransitiveFunction<T> implements Function<T, Set<T>>
{
    private Function<T, Set<T>> directFn;

    public TransitiveFunction(Function<T, Set<T>> directFn)
    {
        this.directFn = directFn;
    }

    public Set<T> apply(T t)
    {
        Set<T> result = new HashSet<T>();
        addAll(directFn, t, result);
        return result;
    }

    private void addAll(Function<T, Set<T>> directFn, T t, Set<T> result)
    {
        for (T u: directFn.apply(t))
        {
            if (result.add(u))
            {
                addAll(directFn, u, result);
            }
        }
    }
}
