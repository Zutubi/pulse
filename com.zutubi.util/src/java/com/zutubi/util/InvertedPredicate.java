package com.zutubi.util;

/**
 * A {@link com.zutubi.util.Predicate} that inverts the results of a delegate
 * predicate.
 */
public class InvertedPredicate<T> implements Predicate<T>
{
    private Predicate<T> delegate;

    /**
     * Create a new predicate that will invert the results of the given
     * delegate.
     *
     * @param delegate delegate predicate to invert
     */
    public InvertedPredicate(Predicate<T> delegate)
    {
        this.delegate = delegate;
    }

    public boolean satisfied(T t)
    {
        return !delegate.satisfied(t);
    }
}