package com.zutubi.pulse.tove.config.agent;

import com.zutubi.pulse.tove.config.group.AbstractGroupConfiguration;

/**
 */
public class AgentAclConfigurationFormatter
{
    public String getGroup(AgentAclConfiguration configuration)
    {
        AbstractGroupConfiguration group = configuration.getGroup();
        return group == null ? null : group.getName();
    }

    public String getAllowedActions(AgentAclConfiguration configuration)
    {
        return configuration.getAllowedActions().toString();
    }
}
