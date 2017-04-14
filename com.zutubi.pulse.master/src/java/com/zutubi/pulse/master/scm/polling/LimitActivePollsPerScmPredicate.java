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

package com.zutubi.pulse.master.scm.polling;

import com.google.common.base.Predicate;
import com.zutubi.pulse.master.model.Project;

import java.util.Map;

/**
 * This predicate ensures that a limited number of poll request can be activated for a single scm
 * (unique scm uid) at a particular point in time.  This is to help prevent hammering a single
 * server with concurrent requests.
 */
public class LimitActivePollsPerScmPredicate implements Predicate<PollingRequest>
{
    private final int limit;
    private final PollingQueue requestQueue;
    private final Map<Long, String> projectUidCache;

    public LimitActivePollsPerScmPredicate(int limit, PollingQueue requestQueue, Map<Long, String> projectUidCache)
    {
        this.limit = limit;
        this.requestQueue = requestQueue;
        this.projectUidCache = projectUidCache;
    }

    public boolean apply(PollingRequest request)
    {
        String uid = getProjectsScmServerUid(request.getProject());
        if (uid == null)
        {
            // essentially disable this predicate if we dont know the scm server uid.
            return true;
        }

        int activeCount = 0;
        PollingQueueSnapshot snapshot = requestQueue.getSnapshot();
        for (PollingRequest activeRequest : snapshot.getActivatedRequests())
        {
            String activeRequestUid = getProjectsScmServerUid(activeRequest.getProject());
            if (uid.equals(activeRequestUid))
            {
                activeCount++;
                if (activeCount == limit)
                {
                    return false;
                }
            }
        }
        return true;
    }

    private String getProjectsScmServerUid(Project project)
    {
        return projectUidCache.get(project.getId());
    }

    @Override
    public String toString()
    {
        return "LimitActivePolls(" + limit + ")";
    }
}
