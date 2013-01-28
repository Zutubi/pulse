package com.zutubi.pulse.master.scheduling;

import com.google.common.base.Predicate;

/**
 * A predicate that is satisfied by any trigger with a specified project id
 */
public class HasProjectPredicate implements Predicate<Trigger>
{
    private long projectId;

    public HasProjectPredicate(long projectId)
    {
        this.projectId = projectId;
    }

    public boolean apply(Trigger trigger)
    {
        return trigger.getProjectId() == projectId;
    }
}
