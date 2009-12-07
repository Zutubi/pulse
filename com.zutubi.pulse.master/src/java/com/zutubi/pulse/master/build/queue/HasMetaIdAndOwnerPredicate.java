package com.zutubi.pulse.master.build.queue;

import com.zutubi.util.Predicate;
import com.zutubi.util.ConjunctivePredicate;

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
        this.delegate = new ConjunctivePredicate<T>(
                new HasMetaIdPredicate(metaBuildId),
                new HasOwnerPredicate(owner)
        );
    }

    public boolean satisfied(T holder)
    {
        return delegate.satisfied(holder);
    }
}
