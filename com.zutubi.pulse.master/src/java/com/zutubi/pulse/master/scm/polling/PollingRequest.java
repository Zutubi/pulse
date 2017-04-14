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
import com.google.common.base.Predicates;
import com.zutubi.pulse.master.model.Project;

/**
 * A request to poll a particular project.  This request has a list of
 * associated predicates which must all be satisfied before this requests
 * project can be polled.
 */
public class PollingRequest
{
    private Project project;
    private ProjectPollingState state;
    private Predicate<PollingRequest> predicate;

    /**
     * Creates a new request to poll the given project when the given
     * predicates are satisfied.
     *
     * @param project the project to poll
     * @param state state of the project at the last poll
     * @param predicates predicates this request must satisfy before it can be
     *                   activated
     */
    public PollingRequest(Project project, ProjectPollingState state, Predicate<PollingRequest>... predicates)
    {
        this.project = project;
        this.state = state;
        predicate = Predicates.and(predicates);
    }

    /**
     * @return the project that a poll is requested for
     */
    public Project getProject()
    {
        return project;
    }

    /**
     * @return the state of the project from the last poll
     */
    public ProjectPollingState getState()
    {
        return state;
    }

    /**
     * Returns true if and only if all of this request's predicates are
     * satisfied.
     *
     * @return true if the predicates are satisfied, false otherwise.
     */
    public boolean satisfied()
    {
        return predicate.apply(this);
    }

    @Override
    public String toString()
    {
        return "poll(" + project + ")";
    }
}
