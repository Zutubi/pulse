package com.zutubi.pulse.master.scheduling;

import com.google.common.base.Predicate;

/**
 * A predicate that is satisfied by any trigger with a specified name.
 */
public class HasNamePredicate implements Predicate<Trigger>
{
    private String name;

    public HasNamePredicate(String name)
    {
        this.name = name;
    }

    public boolean apply(Trigger trigger)
    {
        return trigger.getName().equals(name);
    }
}
