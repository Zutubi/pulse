package com.zutubi.pulse.master.build.queue;

/**
 * This predicate is satisfied by a queued request that has a pending dependency.
 *
 * @see QueuedRequest#isDependencyPending()
 */
public class HasPendingDependencyPredicate implements QueuedRequestPredicate
{
    public boolean apply(QueuedRequest queuedRequest)
    {
        return queuedRequest.isDependencyPending();
    }
}
