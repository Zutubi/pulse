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

import static com.google.common.collect.Iterables.find;
import com.google.common.collect.Lists;

import java.util.LinkedList;

/**
 * This predicate ensures that the queued request is at the front of the
 * list of queued requests for the same owner.
 */
public class HeadOfOwnerQueuePredicate implements QueuedRequestPredicate
{
    private BuildQueue buildQueue;

    public HeadOfOwnerQueuePredicate(BuildQueue buildQueue)
    {
        if (buildQueue == null)
        {
            throw new IllegalArgumentException();
        }
        this.buildQueue = buildQueue;
    }

    /**
     * Returns true if and only if the specified request is at the head of the
     * current queued requests queue for the queued requests owner.
     *
     * @param request   the request being examined.
     * 
     * @return true if the request is at the head of the queued requests queue,
     * false otherwise.
     */
    public boolean apply(final QueuedRequest request)
    {
        LinkedList<QueuedRequest> queuedRequests = new LinkedList<QueuedRequest>(buildQueue.getQueuedRequests());

        // are we at the head of the queue for our owner?
        QueuedRequest headOfQueue = find(
                Lists.reverse(queuedRequests),
                new HasOwnerPredicate<QueuedRequest>(request.getOwner())
        );
        return headOfQueue.equals(request);
    }
}