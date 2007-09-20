package com.zutubi.pulse.prototype.config.group;

import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.Reference;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Transient;
import com.zutubi.pulse.core.config.AbstractNamedConfiguration;
import com.zutubi.pulse.prototype.config.user.UserConfiguration;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a group of users.  Groups are used to conveniently assign
 * permissions to multiple users.
 */
@SymbolicName("zutubi.groupConfig")
@Form(fieldOrder = {"name", "members", "serverPermissions"})
public class GroupConfiguration extends AbstractNamedConfiguration
{
    @Reference
    private List<UserConfiguration> members = new LinkedList<UserConfiguration>();
    private List<ServerPermission> serverPermissions = new LinkedList<ServerPermission>();

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

    public List<ServerPermission> getServerPermissions()
    {
        return serverPermissions;
    }

    public void setServerPermissions(List<ServerPermission> serverPermissions)
    {
        this.serverPermissions = serverPermissions;
    }

    public void addServerPermission(ServerPermission permission)
    {
        serverPermissions.add(permission);
    }

    @Transient
    public String getDefaultAuthority()
    {
        return "group:" + getName();
    }

    @Transient
    public String[] getGrantedAuthorities()
    {
        String[] result = new String[serverPermissions.size() + 1];
        int i = 0;
        for(ServerPermission perm: serverPermissions)
        {
            result[i++] = perm.toString();
        }

        result[i] = getDefaultAuthority();
        return result;
    }
}
