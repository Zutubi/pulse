package com.zutubi.pulse.master.scheduling;

import com.zutubi.util.Predicate;
import com.zutubi.util.ConjunctivePredicate;

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
        this.delegate = new ConjunctivePredicate<Trigger>(
                new HasNamePredicate(name),
                new HasGroupPredicate(group)
        );
    }

    public boolean satisfied(Trigger trigger)
    {
        return delegate.satisfied(trigger);
    }
}
