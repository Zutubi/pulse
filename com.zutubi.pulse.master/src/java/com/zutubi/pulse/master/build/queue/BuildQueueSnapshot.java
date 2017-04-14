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

import com.google.common.collect.Iterables;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

/**
 * The build snapshot is an immutable snapshot of the contents of the build queue
 * at the time it was requested.
 */
public class BuildQueueSnapshot
{
    private List<ActivatedRequest> activatedRequests = new LinkedList<ActivatedRequest>();
    private List<QueuedRequest> queuedRequests = new LinkedList<QueuedRequest>();

    /**
     * The time at which the snapshot was taken (pulse server time).
     */
    private long timestamp;

    public BuildQueueSnapshot()
    {
        timestamp = System.currentTimeMillis();
    }

    /**
     * The list of active build requests.
     *
     * @return the immutable list of active build requests.
     */
    public List<BuildRequestEvent> getActivatedBuildRequests()
    {
        return newArrayList(transform(activatedRequests, new ExtractRequestFunction<ActivatedRequest>()));
    }

    /**
     * The list of activated requests.
     *
     * @return the immutable list of activated requests.
     */
    public List<ActivatedRequest> getActivatedRequests()
    {
        return Collections.unmodifiableList(activatedRequests);
    }

    /**
     * The list of queued build requests.
     *
     * @return the immutable list of queued build requests.
     */
    public List<BuildRequestEvent> getQueuedBuildRequests()
    {
        return newArrayList(transform(queuedRequests, new ExtractRequestFunction<QueuedRequest>()));
    }

    /**
     * The list of queued requests.
     *
     * @return the immutable list of queued requests.
     */
    public List<QueuedRequest> getQueuedRequests()
    {
        return Collections.unmodifiableList(queuedRequests);
    }

    /**
     * The list of queued requests for the specified owner.
     *
     * @param owner     the owner of the requests to be returned.
     * @return the immutable list of queued requests for the specified owner.
     */
    public List<QueuedRequest> getQueuedRequestsByOwner(Object owner)
    {
        return newArrayList(Iterables.filter(queuedRequests, new HasOwnerPredicate<QueuedRequest>(owner)));
    }

    /**
     * Get the time at which the snapshot was taken.
     *
     * @return a timestamp of when the snapshot was taken, in
     * milliseconds since epoch.
     */
    public long getTimestamp()
    {
        return timestamp;
    }

    public void addAllQueuedRequests(LinkedList<QueuedRequest> queuedRequests)
    {
        this.queuedRequests.addAll(queuedRequests);
    }

    public void addAllActivatedRequests(LinkedList<ActivatedRequest> activatedRequests)
    {
        this.activatedRequests.addAll(activatedRequests);
    }
}
