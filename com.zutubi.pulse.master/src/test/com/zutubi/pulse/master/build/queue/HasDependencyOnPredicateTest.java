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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import java.util.Arrays;

public class HasDependencyOnPredicateTest extends BaseQueueTestCase
{
    private BuildQueue buildQueue;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        buildQueue = mock(BuildQueue.class);
    }

    public void testDependencies()
    {
        Project owner = createProject("a");

        HasDependencyOnPredicate predicate = new HasDependencyOnPredicate(buildQueue, owner);

        QueuedRequest r1 = new QueuedRequest(createRequest("b"));
        QueuedRequest r2 = new QueuedRequest(createRequest("c"));
        QueuedRequest r3 = new QueuedRequest(createRequest("d"));
        QueuedRequest r4 = new QueuedRequest(createRequest("e"));

        addDependency(r1, owner);
        addDependency(r2, r1.getOwner());
        addDependency(r3, r2.getOwner());

        stub(buildQueue.getQueuedRequests()).toReturn(Arrays.asList(r4, r3, r2, r1));

        assertTrue(predicate.apply(r1));
        assertTrue(predicate.apply(r2));
        assertTrue(predicate.apply(r3));
        assertFalse(predicate.apply(r4));
    }

    private void addDependency(QueuedRequest request, Object dependentOwner)
    {
        request.addPredicate(new DependencyCompleteQueuePredicate(buildQueue, dependentOwner));
    }
}
