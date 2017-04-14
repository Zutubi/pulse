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

import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;

public class DependencyCompleteQueuePredicateTest extends BaseQueueTestCase
{
    private DependencyCompleteQueuePredicate predicate;
    private Project ownerA;
    private Project ownerB;
    private BuildQueue buildQueue;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        buildQueue = objectFactory.buildBean(BuildQueue.class);
        ownerA = createProject("projectA");
        ownerB = createProject("projectB");
    }

    public void testOwnerQueued()
    {
        BuildRequestEvent requestEvent = createRequest(ownerA);
        buildQueue.enqueue(queue(requestEvent));

        QueuedRequest qr = new QueuedRequest(createRequest(ownerB), new DependencyCompleteQueuePredicate(buildQueue, ownerA));
        assertFalse(qr.satisfied());
    }

    public void testOwnerActivated()
    {
        BuildRequestEvent requestEvent = createRequest(ownerA);
        buildQueue.enqueue(active(requestEvent));

        QueuedRequest qr = new QueuedRequest(createRequest(ownerB), new DependencyCompleteQueuePredicate(buildQueue, ownerA));
        assertFalse(qr.satisfied());
    }

    public void testOwnerNotInQueue()
    {
        QueuedRequest qr = new QueuedRequest(createRequest(ownerB), new DependencyCompleteQueuePredicate(buildQueue, ownerA));
        assertTrue(qr.satisfied());
    }
}
