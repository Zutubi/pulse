package com.zutubi.pulse.master.build.queue;

import com.zutubi.util.Predicate;

/**
 * A build request holder predicate that tests if the build has commenced.
 */
public class BuildHasCommencedPredicate implements Predicate<RequestHolder>
{
    public boolean satisfied(RequestHolder requestHolder)
    {
        return requestHolder instanceof ActivatedRequest && ((ActivatedRequest) requestHolder).isBuildCommenced();
    }
}
