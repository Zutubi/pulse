package com.zutubi.pulse.master.scheduling;

import com.zutubi.util.Predicate;
import com.zutubi.util.ConjunctivePredicate;

/**
 * A predicate that is satisfied by any trigger with a specified name and group.
 * 
 * @see TriggerByNamePredicate
 * @see TriggerByGroupPredicate
 */
public class TriggerByNameAndGroupPredicate implements Predicate<Trigger>
{
    private Predicate<Trigger> delegate;

    public TriggerByNameAndGroupPredicate(String name, String group)
    {
        this.delegate = new ConjunctivePredicate<Trigger>(
                new TriggerByNamePredicate(name),
                new TriggerByGroupPredicate(group)
        );
    }

    public boolean satisfied(Trigger trigger)
    {
        return delegate.satisfied(trigger);
    }
}
