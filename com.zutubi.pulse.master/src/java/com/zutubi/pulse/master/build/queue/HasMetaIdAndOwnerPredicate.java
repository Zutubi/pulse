package com.zutubi.pulse.master.build.queue;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * A predicate that matches a request holder containing a
 * build request event with a specified meta id and owner
 *
 * @param <T> the specific subclass of RequestHolder that is being searched.
 */
public class HasMetaIdAndOwnerPredicate<T extends RequestHolder> implements Predicate<T>
{
    private Predicate<T> delegate;

    public HasMetaIdAndOwnerPredicate(long metaBuildId, Object owner)
    {
        this.delegate = Predicates.and(
                new HasMetaIdPredicate<T>(metaBuildId),
                new HasOwnerPredicate<T>(owner)
        );
    }

    public boolean apply(T holder)
    {
        return delegate.apply(holder);
    }
}
