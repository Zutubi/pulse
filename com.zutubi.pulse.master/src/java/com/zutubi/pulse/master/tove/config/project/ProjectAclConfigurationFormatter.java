package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.master.tove.config.group.GroupConfiguration;
import com.zutubi.util.Sort;

import java.util.Collections;
import java.util.List;

/**
 */
public class ProjectAclConfigurationFormatter
{
    public String getGroup(ProjectAclConfiguration configuration)
    {
        GroupConfiguration group = configuration.getGroup();
        return group == null ? null : group.getName();
    }

    public String getAllowedActions(ProjectAclConfiguration configuration)
    {
        List<String> allowedActions = configuration.getAllowedActions();
        Collections.sort(allowedActions, new Sort.StringComparator());
        return allowedActions.toString();
    }
}
