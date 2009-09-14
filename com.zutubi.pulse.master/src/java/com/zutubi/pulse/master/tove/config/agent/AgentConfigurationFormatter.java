package com.zutubi.pulse.master.tove.config.agent;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.agent.Host;
import com.zutubi.pulse.master.agent.HostManager;
import com.zutubi.pulse.master.model.HostState;
import com.zutubi.util.EnumUtils;

/**
 * Formatter for extra agent status fields.
 */
public class AgentConfigurationFormatter
{
    private AgentManager agentManager;
    private HostManager hostManager;

    public String getLocation(AgentConfiguration configuration)
    {
        Host host = hostManager.getHostForAgent(configuration);
        return host.getLocation();
    }

    public String getStatus(AgentConfiguration configuration)
    {
        // TODO i18n
        Agent agent = agentManager.getAgent(configuration);
        return getStatus(agent);
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
                return messages.format("host.upgrading", new Object[]{EnumUtils.toPrettyString(host.getUpgradeState())});
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

    public void setHostManager(HostManager hostManager)
    {
        this.hostManager = hostManager;
    }
}
