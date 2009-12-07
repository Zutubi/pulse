package com.zutubi.util;

import java.util.Arrays;
import java.util.Collection;

/**
 * A predicate that combines a set of child predicates with 'and';
 */
public class ConjunctivePredicate<T> implements Predicate<T>
{
    private Collection<Predicate<T>> delegates;

    public ConjunctivePredicate(Predicate<T>... delegates)
    {
        this.delegates = Arrays.asList(delegates);
    }

    public ConjunctivePredicate(Collection<Predicate<T>> delegates)
    {
        this.delegates = delegates;
    }

    public boolean satisfied(T t)
    {
        for (Predicate<T> d: delegates)
        {
            if (!d.satisfied(t))
            {
                return false;
            }
        }

        return true;
    }
}
