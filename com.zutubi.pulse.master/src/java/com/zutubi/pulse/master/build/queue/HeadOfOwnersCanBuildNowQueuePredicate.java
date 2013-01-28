package com.zutubi.pulse.master.build.queue;

import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Lists.reverse;

import java.util.LinkedList;

/**
 * This queue predicate is similar to the {@link HeadOfOwnerQueuePredicate} with
 * the difference that it ignores items in the queue that are currently waiting
 * on another build to complete before they can trigger.
 * <p/>
 * This means that items can skip ahead in the queue if the requests blocking it
 * are waiting around for something else, effectively 'blocking' any building
 * happening for the owner.
 */
public class HeadOfOwnersCanBuildNowQueuePredicate implements QueuedRequestPredicate
{
    private BuildQueue buildQueue;

    public HeadOfOwnersCanBuildNowQueuePredicate(BuildQueue buildQueue)
    {
        if (buildQueue == null)
        {
            throw new IllegalArgumentException();
        }
        this.buildQueue = buildQueue;
    }

    public boolean apply(QueuedRequest request)
    {
        LinkedList<QueuedRequest> queuedRequests = new LinkedList<QueuedRequest>(buildQueue.getQueuedRequests());

        // search for the first item in the queue that is not waiting on another build.
        QueuedRequest headOfQueue = find(reverse(queuedRequests),
                and(
                        new HasOwnerPredicate<QueuedRequest>(request.getOwner()),
                        not(new HasPendingDependencyPredicate())
                ),
                null);

        return headOfQueue != null && headOfQueue.equals(request);
    }
}
