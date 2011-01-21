package com.zutubi.pulse.master.tove.config.group;

import com.zutubi.tove.annotations.*;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a group of users.  Groups are used to conveniently assign
 * permissions to multiple users.
 */
@SymbolicName("zutubi.groupConfig")
@Form(fieldOrder = {"name", "members", "serverPermissions"})
@Classification(single = "group")
public class UserGroupConfiguration extends GroupConfiguration
{
    @Reference @Ordered(allowReordering = false)
    private List<UserConfiguration> members = new LinkedList<UserConfiguration>();

    public UserGroupConfiguration()
    {
    }

    public UserGroupConfiguration(String name)
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
