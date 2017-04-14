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

package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.master.build.queue.RecipeAssignmentRequest;
import com.zutubi.pulse.master.agent.AgentService;
import com.zutubi.tove.annotations.Transient;

/**
 * An interface for determining if an agent satisfies the requirements to
 * have a recipe dispatched to it.  Requirements are configurable at the
 * stage level.
 */
public interface AgentRequirements
{
    @Transient
    public String getSummary();
    public boolean isFulfilledBy(RecipeAssignmentRequest request, AgentService service);

    /**
     * Get a human readable reason why the specified request could not be
     * fulfilled.  This assumed that {@link #isFulfilledBy(RecipeAssignmentRequest, AgentService)}
     * has returned false.
     *
     * The format of the reason should be a concise to the point statement.
     *
     * @param request   the recipe request providing the context for the
     *                  isFulfillable check.
     *
     * @return a human readable message for why the requirements are not
     * fulfilled in the context of the request.
     */
    @Transient
    String getUnfulfilledReason(RecipeAssignmentRequest request);
}
