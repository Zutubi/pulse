package com.zutubi.pulse.master.scheduling;

import com.zutubi.util.Predicate;

/**
 * A predicate that is satisfied by any trigger with a specified name.
 */
public class TriggerByNamePredicate implements Predicate<Trigger>
{
    private String name;

    public TriggerByNamePredicate(String name)
    {
        this.name = name;
    }

    public boolean satisfied(Trigger trigger)
    {
        return trigger.getName().equals(name);
    }
}
