package com.zutubi.pulse.prototype.config.project;

import com.zutubi.config.annotations.ItemPicker;
import com.zutubi.config.annotations.Reference;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Table;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.prototype.config.group.GroupConfiguration;
import com.zutubi.validation.annotations.Required;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents the authority to perform some action on some group of projects.
 */
@SymbolicName("zutubi.projectAclConfig")
@Table(columns = {"group", "allowedActions"})
public class ProjectAclConfiguration extends AbstractConfiguration
{
    @Reference
    @Required
    private GroupConfiguration group;
    @ItemPicker(optionProvider = "ProjectAuthorityProvider")
    private List<String> allowedActions = new LinkedList<String>();

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
