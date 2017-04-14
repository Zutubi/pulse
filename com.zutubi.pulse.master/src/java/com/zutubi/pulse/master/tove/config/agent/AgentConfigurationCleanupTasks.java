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
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.util.TransactionContext;
import com.zutubi.tove.config.cleanup.RecordCleanupTask;

import java.util.Arrays;
import java.util.List;

/**
 * Adds a custom cleanup task for agent configuration that deletes the
 * agent state.
 */
public class AgentConfigurationCleanupTasks
{
    private AgentStateManager agentStateManager;
    private TransactionContext transactionContext;

    public List<RecordCleanupTask> getTasks(AgentConfiguration instance)
    {
        return Arrays.<RecordCleanupTask>asList(new AgentStateCleanupTask(instance, agentStateManager, transactionContext));
    }

    public void setAgentStateManager(AgentStateManager agentStateManager)
    {
        this.agentStateManager = agentStateManager;
    }

    public void setTransactionContext(TransactionContext transactionContext)
    {
        this.transactionContext = transactionContext;
    }
}
