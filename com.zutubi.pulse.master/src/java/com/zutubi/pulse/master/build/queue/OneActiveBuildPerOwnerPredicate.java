package com.zutubi.pulse.master.build.queue;

import com.zutubi.util.CollectionUtils;

import java.util.List;

/**
 * This predicate ensures that only one request can be activated for a
 * specific owner at a particular point in time.
 */
public class OneActiveBuildPerOwnerPredicate implements QueuedRequestPredicate
{
    private BuildQueue buildQueue;

    public OneActiveBuildPerOwnerPredicate(BuildQueue buildQueue)
    {
        if (buildQueue == null)
        {
            throw new IllegalArgumentException();
        }
        this.buildQueue = buildQueue;
    }

    /**
     * Returns true if an only if the owner of the specified request does
     * not have any currently activated requests.
     *
     * @param request   the request in question
     *
     * @return true if the requests owner has no active requests, false otherwise.
     */
    public boolean satisfied(final QueuedRequest request)
    {
        List<ActivatedRequest> activatedRequests = buildQueue.getActivatedRequests();
        return !CollectionUtils.contains(activatedRequests, new HasOwnerPredicate<ActivatedRequest>(request.getRequest().getOwner()));
    }
}