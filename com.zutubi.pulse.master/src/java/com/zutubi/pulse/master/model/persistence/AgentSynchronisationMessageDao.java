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

package com.zutubi.pulse.master.model.persistence;

import com.zutubi.pulse.master.model.AgentState;
import com.zutubi.pulse.master.model.AgentSynchronisationMessage;

import java.util.List;

/**
 * Provides access to agent synchronisation message entities.
 */
public interface AgentSynchronisationMessageDao extends EntityDao<AgentSynchronisationMessage>
{
    /**
     * Finds all messages for the given agent.
     *
     * @param agentState state of the agent to get the messages for
     * @return all messages for the specified agent
     */
    List<AgentSynchronisationMessage> findByAgentState(AgentState agentState);

    /**
     * Finds all messages with the given status, across all agents.
     *
     * @param status the status to find messages in
     * @return all messages with the given status
     */
    List<AgentSynchronisationMessage> findByStatus(AgentSynchronisationMessage.Status status);

    /**
     * Finds all messages for a given agent with a given status and task type.
     *
     * @param agentState  state of the agent to get messages for
     * @param status      status to filter by
     * @param taskType    type of tasks to filter by
     * @return all messages for the given agent that meet the criteria
     */
    List<AgentSynchronisationMessage> queryMessages(AgentState agentState, AgentSynchronisationMessage.Status status, String taskType);

    /**
     * Deleted all messages for the given agent.
     *
     * @param agentState state of the agent to delete the messages for
     */
    int deleteByAgentState(AgentState agentState);
}