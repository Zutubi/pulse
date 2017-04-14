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
import com.zutubi.pulse.master.model.Project;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

public class ActiveBuildsPerOwnerPredicateTest extends BaseQueueTestCase
{
    private BuildQueue buildQueue;
    private List<ActivatedRequest> activeRequests;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        activeRequests = new LinkedList<ActivatedRequest>();
        buildQueue = mock(BuildQueue.class);

        stub(buildQueue.getActivatedRequests()).toReturn(activeRequests);
    }

    public void testAllowActive()
    {
        activateRequest(createRequest("a"));
        activateRequest(createRequest("a"));

        assertFalse(canActivate(createRequest("a"), 0));
        assertFalse(canActivate(createRequest("a"), 1));
        assertFalse(canActivate(createRequest("a"), 2));
        assertTrue(canActivate(createRequest("a"), 3));
    }

    public void testPredicateBoundToOwner()
    {
        activateRequest(createRequest("a"));
        assertTrue(canActivate(createRequest("b"), 1));
    }

    private boolean canActivate(BuildRequestEvent request, int concurrentBuilds)
    {
        Project project = (Project) request.getOwner();
        project.getConfig().getOptions().setConcurrentBuilds(concurrentBuilds);
        
        QueuedRequestPredicate predicate = new ActiveBuildsPerOwnerPredicate(buildQueue, concurrentBuilds);
        return predicate.apply(new QueuedRequest(request));
    }

    private void activateRequest(BuildRequestEvent request)
    {
        activeRequests.add(new ActivatedRequest(request));
    }
}
