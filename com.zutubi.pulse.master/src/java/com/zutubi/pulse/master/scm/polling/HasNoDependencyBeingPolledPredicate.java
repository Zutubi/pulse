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

    @Override
    public String toString()
    {
        return "HasNoDependencyBeingPolled";
    }
}
