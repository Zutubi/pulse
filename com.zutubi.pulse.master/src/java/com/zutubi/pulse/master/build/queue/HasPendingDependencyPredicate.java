package com.zutubi.pulse.master.build.queue;

import com.zutubi.util.Predicate;

/**
 * This predicate is satisfied by a queued request that has a pending dependency.
 *
 * @see QueuedRequest#isDependencyPending()
 */
public class HasPendingDependencyPredicate implements Predicate<QueuedRequest>
{
    public boolean satisfied(QueuedRequest queuedRequest)
    {
        return queuedRequest.isDependencyPending();
    }
}
