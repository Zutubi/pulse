package com.zutubi.pulse.prototype.config.agent;

import com.zutubi.pulse.prototype.config.group.AbstractGroupConfiguration;

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
