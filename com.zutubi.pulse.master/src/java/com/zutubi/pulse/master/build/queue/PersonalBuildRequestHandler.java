/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
