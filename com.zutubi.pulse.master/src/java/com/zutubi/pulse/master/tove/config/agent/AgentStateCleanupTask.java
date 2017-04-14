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

package com.zutubi.pulse.master.tove.config.agent;

import com.zutubi.pulse.master.model.AgentStateManager;
import com.zutubi.pulse.master.tove.config.DatabaseStateCleanupTaskSupport;
import com.zutubi.pulse.master.util.TransactionContext;

/**
 * Cleans up the state associated with a deleted agent.
 */
public class AgentStateCleanupTask extends DatabaseStateCleanupTaskSupport
{
    private AgentConfiguration agentConfig;
    private AgentStateManager agentStateManager;

    public AgentStateCleanupTask(AgentConfiguration agentConfig, AgentStateManager agentStateManager, TransactionContext transactionContext)
    {
        super(agentConfig.getConfigurationPath(), transactionContext);
        this.agentConfig = agentConfig;
        this.agentStateManager = agentStateManager;
    }

    public void cleanupState()
    {
        agentStateManager.delete(agentConfig.getAgentStateId());
    }
}
