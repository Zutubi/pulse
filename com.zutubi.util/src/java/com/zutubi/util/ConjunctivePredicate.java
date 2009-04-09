package com.zutubi.util;

/**
 * A predicate that combines a set of child predicates with 'and';
 */
public class ConjunctivePredicate<T> implements Predicate<T>
{
    private Predicate<T>[] delegates;

    public ConjunctivePredicate(Predicate<T>... delegates)
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
