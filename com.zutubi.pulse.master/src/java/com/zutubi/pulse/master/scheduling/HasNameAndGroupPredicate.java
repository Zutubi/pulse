package com.zutubi.pulse.master.scheduling;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * A predicate that is satisfied by any trigger with a specified name and group.
 * 
 * @see HasNamePredicate
 * @see HasGroupPredicate
 */
public class HasNameAndGroupPredicate implements Predicate<Trigger>
{
    private Predicate<Trigger> delegate;

    public HasNameAndGroupPredicate(String name, String group)
    {
        this.delegate = Predicates.and(
                new HasNamePredicate(name),
                new HasGroupPredicate(group)
        );
    }

    public boolean apply(Trigger trigger)
    {
        return delegate.apply(trigger);
    }
}
