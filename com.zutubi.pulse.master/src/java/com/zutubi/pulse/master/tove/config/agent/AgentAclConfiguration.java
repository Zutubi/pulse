package com.zutubi.pulse.master.tove.config.agent;

import com.zutubi.config.annotations.ItemPicker;
import com.zutubi.config.annotations.Reference;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Table;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.master.tove.config.group.AbstractGroupConfiguration;
import com.zutubi.validation.annotations.Required;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents the authority to perform some action on some agent.
 */
@SymbolicName("zutubi.agentAclConfig")
@Table(columns = {"group", "allowedActions"})
public class AgentAclConfiguration extends AbstractConfiguration
{
    @Reference
    @Required
    private AbstractGroupConfiguration group;
    @ItemPicker(optionProvider = "AgentAuthorityProvider")
    private List<String> allowedActions = new LinkedList<String>();

    public AgentAclConfiguration()
    {
    }

    public AgentAclConfiguration(AbstractGroupConfiguration group, String... actions)
    {
        this.group = group;
        this.allowedActions.addAll(Arrays.asList(actions));
    }

    public AbstractGroupConfiguration getGroup()
    {
        return group;
    }

    public void setGroup(AbstractGroupConfiguration group)
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
