package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.master.tove.config.group.AbstractGroupConfiguration;
import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.annotations.Required;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents the authority to perform some action on some project.
 */
@SymbolicName("zutubi.projectAclConfig")
@Table(columns = {"group", "allowedActions"})
@Form(fieldOrder = {"group", "allowedActions"})
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
