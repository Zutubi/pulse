package com.zutubi.pulse.master.build.queue;

import com.zutubi.util.CollectionUtils;

import java.util.List;

/**
 * A predicate that requires another build request to be completed to be satisfied.
 */
public class OwnerCompleteQueuePredicate implements QueuedRequestPredicate, OwnerDependencyPredicate
{
    private Object owner;

    private BuildQueue buildQueue;

    public OwnerCompleteQueuePredicate(BuildQueue buildQueue, Object owner)
    {
        if (buildQueue == null)
        {
            throw new IllegalArgumentException();
        }
        if (owner == null)
        {
            throw new IllegalArgumentException();
        }

        this.owner = owner;
        this.buildQueue = buildQueue;
    }

    public Object getOwner()
    {
        return owner;
    }

    public boolean satisfied(QueuedRequest request)
    {
        long metaBuildId = request.getRequest().getMetaBuildId();

        List<RequestHolder> existingRequests = buildQueue.getMetaBuildRequests(metaBuildId);
        return !CollectionUtils.contains(existingRequests, new HasOwnerPredicate(owner));
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OwnerCompleteQueuePredicate that = (OwnerCompleteQueuePredicate) o;

        return buildQueue == that.buildQueue && owner.equals(that.owner);
    }

    @Override
    public int hashCode()
    {
        int result = owner.hashCode();
        result = 31 * result + buildQueue.hashCode();
        return result;
    }
}

