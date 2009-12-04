package com.zutubi.pulse.master.build.queue;

import com.zutubi.pulse.master.events.build.BuildRequestEvent;

import java.util.Arrays;
import java.util.List;

/**
 * A request handler for personal builds.
 *
 * A personal build request triggers a build of the specified project only.
 */
public class PersonalBuildRequestHandler extends BaseBuildRequestHandler
{
    public List<QueuedRequest> prepare(BuildRequestEvent request)
    {
        if (request.getMetaBuildId() != 0)
        {
            throw new IllegalArgumentException("The build request has already been handled by another handler.");
        }
        if (!request.isPersonal())
        {
            throw new IllegalArgumentException("The build request is not a personal build request.");
        }

        request.setMetaBuildId(getMetaBuildId());

        QueuedRequest queuedRequest = new QueuedRequest(request,
                new OneActiveBuildPerOwnerPredicate(buildQueue),
                new HeadOfOwnerQueuePredicate(buildQueue)
        );
        return Arrays.asList(queuedRequest);
    }
}
