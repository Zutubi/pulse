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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import static com.google.common.collect.Iterables.find;

/**
 * A predicate to select only those queued requests that are dependent on
 * a specific owner.  The dependencies are defined by the existence of {@link DependencyPredicate}s
 */
public class HasDependencyOnPredicate implements QueuedRequestPredicate
{
    private BuildQueue buildQueue;
    private Object owner;

    public HasDependencyOnPredicate(BuildQueue buildQueue, Object owner)
    {
        this.buildQueue = buildQueue;
        this.owner = owner;
    }

    public boolean apply(QueuedRequest queuedRequest)
    {
        return dependsOn(queuedRequest, owner);
    }

    private boolean dependsOn(QueuedRequest request, Object owner)
    {
        Iterable<QueuedRequestPredicate> dependencies = extractDependencies(request);

        for (QueuedRequestPredicate predicate : dependencies)
        {
            DependencyPredicate dependency = (DependencyPredicate) predicate;
            if (dependency.getOwner().equals(owner))
            {
                return true;
            }
            else // if this is not the dependency we are looking for, search its transitive dependencies.
            {
                // Resolve the predicate into the associated queued request.  It is the queued
                // request that contains the transitive dependency details via its predicates.
                QueuedRequest queuedRequest = find(buildQueue.getQueuedRequests(),
                        new HasMetaIdAndOwnerPredicate<QueuedRequest>(request.getMetaBuildId(), dependency.getOwner()),
                        null
                );
                if (queuedRequest != null && dependsOn(queuedRequest, owner))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private Iterable<QueuedRequestPredicate> extractDependencies(QueuedRequest request)
    {
        return Iterables.filter(request.getPredicates(), new Predicate<QueuedRequestPredicate>()
        {
            public boolean apply(QueuedRequestPredicate predicate)
            {
                return DependencyPredicate.class.isAssignableFrom(predicate.getClass());
            }
        });
    }
}
