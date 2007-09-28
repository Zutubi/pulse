package com.zutubi.pulse.prototype.config.group;

import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.Reference;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Transient;
import com.zutubi.pulse.prototype.config.user.UserConfiguration;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a group of users.  Groups are used to conveniently assign
 * permissions to multiple users.
 */
@SymbolicName("zutubi.groupConfig")
@Form(fieldOrder = {"name", "members", "serverPermissions"})
public class GroupConfiguration extends AbstractGroupConfiguration
{
    @Reference
    private List<UserConfiguration> members = new LinkedList<UserConfiguration>();

    public GroupConfiguration()
    {
    }

    public GroupConfiguration(String name)
    {
        super(name);
    }

    public List<UserConfiguration> getMembers()
    {
        return members;
    }

    public void setMembers(List<UserConfiguration> members)
    {
        this.members = members;
    }

    @Transient
    public String getDefaultAuthority()
    {
        return "group:" + getName();
    }
}
