package com.zutubi.pulse.master.build.queue;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.ReverseListIterator;

import java.util.LinkedList;
import java.util.List;

/**
 * This predicate ensures that the queued request is at the front of the
 * list of queued requests for the same owner.
 */
public class HeadOfOwnerQueuePredicate implements QueuedRequestPredicate
{
    private BuildQueue buildQueue;

    public HeadOfOwnerQueuePredicate(BuildQueue buildQueue)
    {
        if (buildQueue == null)
        {
            throw new IllegalArgumentException();
        }
        this.buildQueue = buildQueue;
    }

    /**
     * Returns true if and only if the specified request is at the head of the
     * current queued requests queue for the queued requests owner.
     *
     * @param request   the request being examined.
     * 
     * @return true if the request is at the head of the queued requests queue,
     * false otherwise.
     */
    public boolean satisfied(final QueuedRequest request)
    {
        LinkedList<QueuedRequest> queuedRequests = new LinkedList<QueuedRequest>(buildQueue.getQueuedRequests());

        // are we at the head of the queue for our owner?
        QueuedRequest headOfQueue = CollectionUtils.find(
                new ReverseListIterator<QueuedRequest>(queuedRequests),
                new HasOwnerPredicate(request.getOwner())
        );
        return headOfQueue.equals(request);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HeadOfOwnerQueuePredicate that = (HeadOfOwnerQueuePredicate) o;

        return buildQueue == that.buildQueue;
    }

    @Override
    public int hashCode()
    {
        return buildQueue.hashCode();
    }
}