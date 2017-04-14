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

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.agent.Host;
import com.zutubi.pulse.master.agent.HostLocationFormatter;
import com.zutubi.pulse.master.model.HostState;
import com.zutubi.util.EnumUtils;

/**
 * Formatter for extra agent status fields.
 */
public class AgentConfigurationFormatter
{
    private AgentManager agentManager;

    public String getLocation(AgentConfiguration configuration)
    {
        return HostLocationFormatter.format(configuration);
    }

    public String getStatus(AgentConfiguration configuration)
    {
        if (configuration.isConcrete())
        {
            Agent agent = agentManager.getAgent(configuration);
            if (agent != null)
            {
                return getStatus(agent);
            }
        }

        return null;
    }

    public String getStatus(Agent agent)
    {
        Messages messages = Messages.getInstance(AgentConfiguration.class);

        Host host = agent.getHost();
        if (host.isUpgrading())
        {
            if (host.getPersistentUpgradeState() == HostState.PersistentUpgradeState.FAILED_UPGRADE)
            {
                return messages.format("host.upgrade.failed");
            }
            else
            {
                return messages.format("host.upgrading", EnumUtils.toPrettyString(host.getUpgradeState()));
            }
        }
        else
        {
            if (agent.isEnabled())
            {
                if (agent.isDisabling())
                {
                    return messages.format("disable.on.idle");
                }
                return agent.getStatus().getPrettyString();
            }
            else
            {
                return EnumUtils.toPrettyString(agent.getEnableState());
            }
        }
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }
}
