package com.zutubi.pulse.prototype.config.project;

import com.zutubi.config.annotations.ItemPicker;
import com.zutubi.config.annotations.Reference;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Table;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.prototype.config.group.AbstractGroupConfiguration;
import com.zutubi.validation.annotations.Required;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents the authority to perform some action on some project.
 */
@SymbolicName("zutubi.projectAclConfig")
@Table(columns = {"group", "allowedActions"})
public class ProjectAclConfiguration extends AbstractConfiguration
{
    @Reference
    @Required
    private AbstractGroupConfiguration group;
    @ItemPicker(optionProvider = "ProjectAuthorityProvider")
    private List<String> allowedActions = new LinkedList<String>();

    public ProjectAclConfiguration()
    {
    }

    public ProjectAclConfiguration(AbstractGroupConfiguration group, String... allowedActions)
    {
        this.group = group;
        this.allowedActions.addAll(Arrays.asList(allowedActions));
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
