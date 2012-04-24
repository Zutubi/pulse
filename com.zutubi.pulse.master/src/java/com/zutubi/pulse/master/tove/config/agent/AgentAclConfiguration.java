package com.zutubi.pulse.master.tove.config.agent;

import com.zutubi.pulse.master.tove.config.group.GroupConfiguration;
import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.annotations.Required;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents the authority to perform some action on some agent.
 */
@SymbolicName("zutubi.agentAclConfig")
@Table(columns = {"group", "allowedActions"})
@PermissionConfiguration
public class AgentAclConfiguration extends AbstractConfiguration
{
    @Reference
    @Required
    private GroupConfiguration group;
    @ItemPicker(optionProvider = "AgentAuthorityProvider")
    private List<String> allowedActions = new LinkedList<String>();

    public AgentAclConfiguration()
    {
    }

    public AgentAclConfiguration(GroupConfiguration group, String... actions)
    {
        this.group = group;
        this.allowedActions.addAll(Arrays.asList(actions));
    }

    public GroupConfiguration getGroup()
    {
        return group;
    }

    public void setGroup(GroupConfiguration group)
    {
        this.group = group;
    }

    public List<String> getAllowedActions()
    {
        return allowedActions;
    }

    public void setAllowedActions(List<String> allowedActions)
    {
        this.allowedActions = allowedActions;
    }
}
