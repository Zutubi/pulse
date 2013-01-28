package com.zutubi.pulse.master.build.queue;

import com.google.common.base.Predicate;

/**
 * A build request holder predicate that tests if the build has commenced.
 */
public class BuildHasCommencedPredicate implements Predicate<RequestHolder>
{
    public boolean apply(RequestHolder requestHolder)
    {
        return requestHolder instanceof ActivatedRequest && ((ActivatedRequest) requestHolder).isBuildCommenced();
    }
}
