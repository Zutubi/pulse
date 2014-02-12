package com.zutubi.pulse.master.build.queue;

import com.google.common.base.Predicate;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;

import java.util.*;

/**
 * The queued request holds the details of a build request that is
 * currently queued within the build queue, awaiting activation.
 *
 * A queued request can be activated only when all of its predicates
 * are {@link #satisfied()}.
 */
public class QueuedRequest extends RequestHolder
{
    /**
     * The list of predicates that this queued request must satisfy before
     * it can be activated.
     */
    private List<QueuedRequestPredicate> predicates;

    public QueuedRequest(BuildRequestEvent request, List<QueuedRequestPredicate> predicates)
    {
        super(request);
        this.predicates = predicates;
    }

    public QueuedRequest(BuildRequestEvent request, QueuedRequestPredicate... predicates)
    {
        this(request, new LinkedList<QueuedRequestPredicate>(Arrays.asList(predicates)));
    }

    /**
     * Get this queued requests predicates.
     *
     * @return a list of predicates
     */
    public List<QueuedRequestPredicate> getPredicates()
    {
        return predicates;
    }

    /**
     * This method returns true if this queued requests predicates are
     * all satisfied, false otherwise.
     *
     * Only a satisfied queued request can be activated.
     *
     * @return true if all of the predicates are satisfied, false otherwise.
     */
    public boolean satisfied()
    {
        for (Predicate<QueuedRequest> predicate : predicates)
        {
            if (!predicate.apply(this))
            {
                return false;
            }
        }
        return true;
    }

    public void addPredicate(QueuedRequestPredicate predicate)
    {
        this.predicates.add(predicate);
    }

    /**
     * Indicates whether or not this queued request is waiting on a dependency
     * before it can be activated.
     *
     * @return true if we are waiting on a dependency, false otherwise.
     */
    public boolean isDependencyPending()
    {
        for (QueuedRequestPredicate predicate : predicates)
        {
            if (predicate instanceof DependencyPredicate)
            {
                if (!predicate.apply(this))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Collects the owners (projects) from all build requests that this request is directly
     * dependent on.
     *
     * @return the set of all owners that this request depends on builds of
     */
    public Set<Object> getDependentOwners()
    {
        Set<Object> dependentOwners = new HashSet<Object>();
        for (Predicate predicate : predicates)
        {
            if (predicate instanceof DependencyPredicate)
            {
                dependentOwners.add(((DependencyPredicate)predicate).getOwner());
            }
        }
        return dependentOwners;
    }
    
}
