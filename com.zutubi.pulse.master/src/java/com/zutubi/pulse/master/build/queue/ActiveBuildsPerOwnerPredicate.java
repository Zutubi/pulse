package com.zutubi.pulse.master.build.queue;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.size;
import com.zutubi.pulse.core.model.NamedEntity;
import com.zutubi.pulse.master.model.Project;

import java.util.List;

/**
 * This predicate ensures that at most a predefined number of request can be activated
 * for a specific owner at a particular point in time.
 */
public class ActiveBuildsPerOwnerPredicate implements QueuedRequestPredicate
{
    private BuildQueue buildQueue;

    public ActiveBuildsPerOwnerPredicate(BuildQueue buildQueue)
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
    public boolean apply(final QueuedRequest request)
    {
        List<ActivatedRequest> activatedRequests = buildQueue.getActivatedRequests();
        NamedEntity owner = request.getRequest().getOwner();

        int ownerRequests = size(filter(activatedRequests, new HasOwnerPredicate<ActivatedRequest>(owner)));
        
        int allowedActiveBuilds = 1;
        if (owner instanceof Project)
        {
            Project project = (Project) owner;
            allowedActiveBuilds = project.getConfig().getOptions().getConcurrentBuilds();
        }

        return ownerRequests < allowedActiveBuilds;
    }
}