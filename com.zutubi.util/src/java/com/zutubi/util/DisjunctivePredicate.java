package com.zutubi.util;

/**
 * A predicate that combines a set of child predicates with 'or';
 */
public class DisjunctivePredicate<T> implements Predicate<T>
{
    private Predicate<T>[] delegates;

    public DisjunctivePredicate(Predicate<T>... delegates)
    {
        this.delegates = delegates;
    }

    public boolean satisfied(T t)
    {
        for (Predicate<T> d: delegates)
        {
            if (d.satisfied(t))
            {
                return true;
            }
        }

        return false;
    }
}