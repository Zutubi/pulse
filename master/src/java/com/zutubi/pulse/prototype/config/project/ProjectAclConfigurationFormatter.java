package com.zutubi.pulse.prototype.config.project;

import com.zutubi.pulse.prototype.config.group.GroupConfiguration;

/**
 */
public class ProjectAclConfigurationFormatter
{
    public String getGroup(ProjectAclConfiguration configuration)
    {
        GroupConfiguration group = configuration.getGroup();
        return group == null ? null : group.getName();
    }

    public String getAuthorities(ProjectAclConfiguration configuration)
    {
        return configuration.getAuthorities().toString();
    }
}
