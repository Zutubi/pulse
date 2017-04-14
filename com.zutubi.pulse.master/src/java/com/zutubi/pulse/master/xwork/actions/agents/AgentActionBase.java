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

import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.pulse.master.xwork.actions.LookupErrorException;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.util.StringUtils;

/**
 */
public class AgentActionBase extends ActionSupport
{
    private String agentName;
    private long agentId;
    private Agent agent;
    protected AgentManager agentManager;
    private ConfigurationTemplateManager configurationTemplateManager;

    public String getAgentName()
    {
        return agentName;
    }

    public void setAgentName(String agentName)
    {
        this.agentName = agentName;
    }

    public long getAgentId()
    {
        return agentId;
    }

    public void setAgentId(long agentId)
    {
        this.agentId = agentId;
    }

    public String getu_agentName()
    {
        return uriComponentEncode(agentName);
    }

    public String geth_agentName()
    {
        return htmlEncode(agentName);
    }

    public Agent getAgent()
    {
        if(agent == null)
        {
            if (StringUtils.stringSet(agentName))
            {
                agent = agentManager.getAgent(agentName);
                if(agent == null)
                {
                    throw new LookupErrorException("Unknown agent '" + agentName + "'");
                }
                if(!configurationTemplateManager.isDeeplyValid(agent.getConfig().getConfigurationPath()))
                {
                    throw new LookupErrorException("Agent configuration is invalid.");
                }
            }

            if (agentId != 0)
            {
                agent = agentManager.getAgentById(agentId);
                if (agent == null)
                {
                    throw new LookupErrorException("Unknown agent id '" + agentId + "'");
                }
            }
        }

        return agent;
    }

    public Agent getRequiredAgent()
    {
        Agent agent = getAgent();
        if(agent == null)
        {
            throw new LookupErrorException("Agent name is required");
        }

        return agent;
    }

    @Override
    public String execute() throws Exception
    {
        getRequiredAgent();
        return SUCCESS;
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}
