package com.zutubi.pulse.master.tove.config.agent;

import com.zutubi.pulse.master.tove.config.group.GroupConfiguration;

/**
 */
public class AgentAclConfigurationFormatter
{
    public String getGroup(AgentAclConfiguration configuration)
    {
        GroupConfiguration group = configuration.getGroup();
        return group == null ? null : group.getName();
    }

    public String getAllowedActions(AgentAclConfiguration configuration)
    {
        return configuration.getAllowedActions().toString();
    }
}
