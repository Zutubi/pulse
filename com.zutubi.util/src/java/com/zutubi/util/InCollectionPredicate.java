package com.zutubi.util;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Arrays;

/**
 * A {@link Predicate} that returns true for objects equal to members of a
 * given collection.  Members must have proper equals and hashCode behaviour.
 * <p/>
 * Essentially an adaptation of Collection.contains to the predicate interface.
 */
public class InCollectionPredicate<T> implements Predicate<T>
{
    private Set<T> set;

    /**
     * Create a new predicate that will return true for all objects contained
     * within the given collection (as defined by {@link Set#contains}).
     *
     * @param collection collection of objects that pass this predicate
     */
    public InCollectionPredicate(Collection<T> collection)
    {
        this.set = new HashSet<T>(collection);
    }

    /**
     * Convenience constructor equivalent to this(Arrays.asList(array)).
     *
     * @param array array of objects that pass this predicate
     */
    public InCollectionPredicate(T... array)
    {
        this(Arrays.asList(array));
    }

    public boolean satisfied(T t)
    {
        return set.contains(t);
    }
}