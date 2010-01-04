package com.zutubi.pulse.master.scm.polling;

import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

/**
 * This predicate is satisfied if the project in question has
 * no dependencies that are currently being polled.  This includes
 * both queued and active polling requests.
 */
public class HasNoDependencyBeingPolledPredicate implements Predicate<PollingRequest>
{
    private PollingQueue requestQueue;

    public HasNoDependencyBeingPolledPredicate(PollingQueue requestQueue)
    {
        this.requestQueue = requestQueue;
    }

    public boolean satisfied(PollingRequest request)
    {
        PollingQueueSnapshot snapshot = requestQueue.getSnapshot();

        final ProjectConfiguration projectBeingTested = request.getProject().getConfig();

        // predicate is satisfied if it locates a project the project being tested is
        // dependent upon.
        Predicate<PollingRequest> hasDependencyPredicate = new Predicate<PollingRequest>()
        {
            public boolean satisfied(PollingRequest t)
            {
                ProjectConfiguration otherProject = t.getProject().getConfig();
                return projectBeingTested.isDependentOn(otherProject);
            }
        };

        return !CollectionUtils.contains(snapshot.getActivatedRequests(), hasDependencyPredicate) &&
                !CollectionUtils.contains(snapshot.getQueuedRequests(), hasDependencyPredicate);
    }
}
