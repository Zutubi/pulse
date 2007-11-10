package com.zutubi.pulse.prototype.config.group;

import com.zutubi.config.annotations.*;
import com.zutubi.pulse.prototype.config.user.UserConfiguration;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a group of users.  Groups are used to conveniently assign
 * permissions to multiple users.
 */
@SymbolicName("zutubi.groupConfig")
@Form(fieldOrder = {"name", "members", "serverPermissions"})
@Classification(single = "group")
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
