package com.zutubi.pulse.master.scheduling;

import com.zutubi.util.Predicate;

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

    public boolean satisfied(Trigger trigger)
    {
        return trigger.getProjectId() == projectId;
    }
}
