package com.zutubi.pulse.master.build.queue;

import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.model.UserManager;

import java.util.Arrays;
import java.util.List;

/**
 * A request handler for personal builds.
 *
 * A personal build request triggers a build of the specified project only.
 */
public class PersonalBuildRequestHandler extends BaseBuildRequestHandler
{
    private UserManager userManager;

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
                new ActiveBuildsPerOwnerPredicate(buildQueue, userManager.getConcurrentPersonalBuilds((User) request.getOwner())),
                new HeadOfOwnerQueuePredicate(buildQueue)
        );
        return Arrays.asList(queuedRequest);
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }
}
