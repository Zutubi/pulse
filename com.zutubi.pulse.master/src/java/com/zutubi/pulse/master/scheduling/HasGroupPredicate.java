package com.zutubi.pulse.master.scheduling;

import com.google.common.base.Predicate;

/**
 * A predicate that is satisfied by any trigger with a specified group.
 */
public class HasGroupPredicate implements Predicate<Trigger>
{
    private String group;

    public HasGroupPredicate(String group)
    {
        this.group = group;
    }

    public boolean apply(Trigger trigger)
    {
        return trigger.getGroup().equals(group);
    }
}
