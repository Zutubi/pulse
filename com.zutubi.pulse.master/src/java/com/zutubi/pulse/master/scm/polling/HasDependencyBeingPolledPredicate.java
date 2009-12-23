package com.zutubi.pulse.master.scm.polling;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.scm.util.PredicateRequest;
import com.zutubi.pulse.master.scm.util.PredicateRequestQueue;
import com.zutubi.pulse.master.scm.util.PredicateRequestQueueSnapshot;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

/**
 * This predicate is satisfied if the project in question has
 * a dependency that is currently being polled.  This includes
 * both queued and active polling requests.
 */
public class HasDependencyBeingPolledPredicate implements Predicate<PredicateRequest<Project>>
{
    private PredicateRequestQueue<Project> requestQueue;

    public HasDependencyBeingPolledPredicate(PredicateRequestQueue<Project> requestQueue)
    {
        this.requestQueue = requestQueue;
    }

    public boolean satisfied(final PredicateRequest<Project> request)
    {
        PredicateRequestQueueSnapshot<Project> snapshot = requestQueue.getSnapshot();

        final ProjectConfiguration project = request.getData().getConfig();

        Predicate<PredicateRequest<Project>> hasDependencyPredicate = new Predicate<PredicateRequest<Project>>()
        {
            public boolean satisfied(PredicateRequest<Project> request)
            {
                return project.isDependentOn(request.getData().getConfig());
            }
        };

        return CollectionUtils.contains(snapshot.getActivatedRequests(), hasDependencyPredicate) ||
                CollectionUtils.contains(snapshot.getQueuedRequests(), hasDependencyPredicate);
    }
}
