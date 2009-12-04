package com.zutubi.util;

import java.util.List;
import java.util.Arrays;

/**
 * A predicate that combines a set of child predicates with 'and';
 */
public class ConjunctivePredicate<T> implements Predicate<T>
{
    private List<Predicate<T>> delegates;

    public ConjunctivePredicate(Predicate<T>... delegates)
    {
        this.delegates = Arrays.asList(delegates);
    }

    public ConjunctivePredicate(List<Predicate<T>> delegates)
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
