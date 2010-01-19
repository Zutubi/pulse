package com.zutubi.pulse.master.scm.polling;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.util.Predicate;

import java.util.Map;

/**
 * This predicate ensures that only one poll request can be activated for a
 * specific scm (unique scm uid) at a particular point in time.
 */
public class OneActivePollPerScmPredicate implements Predicate<PollingRequest>
{
    private PollingQueue requestQueue;
    private Map<Long, String> projectUidCache;

    public OneActivePollPerScmPredicate(PollingQueue requestQueue, Map<Long, String> projectUidCache)
    {
        this.requestQueue = requestQueue;
        this.projectUidCache = projectUidCache;
    }

    public boolean satisfied(PollingRequest request)
    {
        String uid = getProjectsScmServerUid(request.getProject());
        if (uid == null)
        {
            // essentially disable this predicate if we dont know the scm server uid.
            return true;
        }

        PollingQueueSnapshot snapshot = requestQueue.getSnapshot();
        for (PollingRequest activeRequest : snapshot.getActivatedRequests())
        {
            String activeRequestUid = getProjectsScmServerUid(activeRequest.getProject());
            if (uid.equals(activeRequestUid))
            {
                return false;
            }
        }
        return true;
    }

    private String getProjectsScmServerUid(Project project)
    {
        return projectUidCache.get(project.getId());
    }
}
