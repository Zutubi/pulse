package com.zutubi.pulse.master.build.queue;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.ConjunctivePredicate;
import com.zutubi.util.ReverseListIterator;
import com.zutubi.util.InvertedPredicate;

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

    public boolean satisfied(QueuedRequest request)
    {
        LinkedList<QueuedRequest> queuedRequests = new LinkedList<QueuedRequest>(buildQueue.getQueuedRequests());

        // search for the first item in the queue that is not waiting on another build.
        QueuedRequest headOfQueue = CollectionUtils.find(new ReverseListIterator<QueuedRequest>(queuedRequests),
                new ConjunctivePredicate<QueuedRequest>(
                        new HasOwnerPredicate<QueuedRequest>(request.getOwner()),
                        new InvertedPredicate<QueuedRequest>(new HasPendingDependencyPredicate())
        ));

        return headOfQueue != null && headOfQueue.equals(request);
    }
}
