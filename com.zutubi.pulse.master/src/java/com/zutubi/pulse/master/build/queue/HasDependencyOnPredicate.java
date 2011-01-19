package com.zutubi.pulse.master.build.queue;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.util.List;

/**
 * A predicate to select only those queued requests that are dependent on
 * a specific owner.  The dependencies are defined by the existence of {@link DependencyPredicate}s
 */
public class HasDependencyOnPredicate implements QueuedRequestPredicate
{
    private BuildQueue buildQueue;
    private Object owner;

    public HasDependencyOnPredicate(BuildQueue buildQueue, Object owner)
    {
        this.buildQueue = buildQueue;
        this.owner = owner;
    }

    public boolean satisfied(QueuedRequest queuedRequest)
    {
        return dependsOn(queuedRequest, owner);
    }

    private boolean dependsOn(QueuedRequest request, Object owner)
    {
        List<QueuedRequestPredicate> dependencies = extractDependencies(request);

        for (QueuedRequestPredicate predicate : dependencies)
        {
            DependencyPredicate dependency = (DependencyPredicate) predicate;
            if (dependency.getOwner().equals(owner))
            {
                return true;
            }
            else // if this is not the dependency we are looking for, search its transitive dependencies.
            {
                // Resolve the predicate into the associated queued request.  It is the queued
                // request that contains the transitive dependency details via its predicates.
                QueuedRequest queuedRequest = CollectionUtils.find(buildQueue.getQueuedRequests(),
                        new HasMetaIdAndOwnerPredicate(request.getMetaBuildId(), dependency.getOwner())
                );
                if (queuedRequest != null && dependsOn(queuedRequest, owner))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private List<QueuedRequestPredicate> extractDependencies(QueuedRequest request)
    {
        return CollectionUtils.filter(request.getPredicates(), new Predicate<QueuedRequestPredicate>()
        {
            public boolean satisfied(QueuedRequestPredicate predicate)
            {
                return DependencyPredicate.class.isAssignableFrom(predicate.getClass());
            }
        });
    }
}
