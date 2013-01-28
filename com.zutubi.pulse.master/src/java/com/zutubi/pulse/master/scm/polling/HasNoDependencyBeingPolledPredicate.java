package com.zutubi.pulse.master.scm.polling;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

/**
 * This predicate is satisfied if the project in question has
 * no dependencies that are currently being polled.  This includes
 * both queued and active polling requests.
 */
public class HasNoDependencyBeingPolledPredicate implements Predicate<PollingRequest>
{
    private final PollingQueue requestQueue;

    public HasNoDependencyBeingPolledPredicate(PollingQueue requestQueue)
    {
        this.requestQueue = requestQueue;
    }

    public boolean apply(PollingRequest request)
    {
        PollingQueueSnapshot snapshot = requestQueue.getSnapshot();

        final ProjectConfiguration projectBeingTested = request.getProject().getConfig();

        // predicate is satisfied if it locates a project the project being tested is
        // dependent upon.
        Predicate<PollingRequest> hasDependencyPredicate = new Predicate<PollingRequest>()
        {
            public boolean apply(PollingRequest t)
            {
                ProjectConfiguration otherProject = t.getProject().getConfig();
                return projectBeingTested.isDependentOn(otherProject);
            }
        };

        return !Iterables.any(snapshot.getActivatedRequests(), hasDependencyPredicate) &&
                !Iterables.any(snapshot.getQueuedRequests(), hasDependencyPredicate);
    }
}
