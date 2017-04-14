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

package com.zutubi.pulse.master.xwork.actions.agents;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.agent.Agent;

/**
 * Action to return JSON data for the agent statistics tab.
 */
public class AgentStatisticsDataAction extends AgentActionBase
{
    private static final Messages I18N = Messages.getInstance(AgentStatisticsDataAction.class);

    private AgentStatisticsModel model;

    public AgentStatisticsModel getModel()
    {
        return model;
    }

    @Override
    public String execute() throws Exception
    {
        Agent agent = getRequiredAgent();
        model = new AgentStatisticsModel(agentManager.getAgentStatistics(agent));
        return SUCCESS;
    }
}
