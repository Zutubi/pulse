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

package com.zutubi.pulse.master.events;

import com.zutubi.pulse.master.agent.Agent;

/**
 * Raised when synchronisation is complete for an agent: i.e. it is ready to
 * move to the idle state.
 */
public class AgentSynchronisationCompleteEvent extends AgentEvent
{
    private boolean successful;

    /**
     * Creates a new event indicating that an agent sync cycle has completed.
     *
     * @param source     source of the event
     * @param agent      the agent that was synchronising
     * @param successful true if the sync cycle was successful - there are
     *                   subtelties around this, see {@link #isSuccessful()}
     *                   for details
     */
    public AgentSynchronisationCompleteEvent(Object source, Agent agent, boolean successful)
    {
        super(source, agent);
        this.successful = successful;
    }

    /**
     * Indicates if the sync cycle was successful -- i.e. it did not encounter
     * any temporary errors talking to the agent.  Note that this is <b>not</b>
     * the same as saying all synchronisation tasks succeeded.  If tasks fail
     * in a fatal way that may just be logged and the cycle still considered
     * successful.  A failed cycle indicates that some tasks could not even be
     * attempted - presumably due to connectivity issues - and that they should
     * be retried.
     *
     * @return true if the synchronisation cycle succeeded
     */
    public boolean isSuccessful()
    {
        return successful;
    }

    @Override
    public String toString()
    {
        return "Agent Synchronisation Complete Event: " + getAgent().getName() + ": " + (successful ? "success" : "failure");
    }
}
