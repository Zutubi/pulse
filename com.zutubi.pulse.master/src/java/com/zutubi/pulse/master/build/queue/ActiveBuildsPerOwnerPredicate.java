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

import com.zutubi.pulse.core.model.NamedEntity;

import java.util.List;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.size;

/**
 * This predicate ensures that at most a predefined number of request can be activated
 * for a specific owner at a particular point in time.
 */
public class ActiveBuildsPerOwnerPredicate implements QueuedRequestPredicate
{
    private BuildQueue buildQueue;
    private int allowedActiveBuilds;

    public ActiveBuildsPerOwnerPredicate(BuildQueue buildQueue, int allowedActiveBuilds)
    {
        if (buildQueue == null)
        {
            throw new IllegalArgumentException();
        }
        this.buildQueue = buildQueue;
        this.allowedActiveBuilds = allowedActiveBuilds;
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
        return ownerRequests < allowedActiveBuilds;
    }
}