package com.zutubi.pulse.prototype.config.group;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Table;
import com.zutubi.config.annotations.Transient;
import com.zutubi.config.annotations.Classification;
import com.zutubi.pulse.core.config.AbstractNamedConfiguration;

import java.util.LinkedList;
import java.util.List;

/**
 */
@SymbolicName("zutubi.abstractGroupConfig")
@Table(columns = {"name", "members"})
@Classification(collection = "users")
public abstract class AbstractGroupConfiguration extends AbstractNamedConfiguration
{
    private List<ServerPermission> serverPermissions = new LinkedList<ServerPermission>();

    public AbstractGroupConfiguration()
    {
    }

    public AbstractGroupConfiguration(String name)
    {
        super(name);
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

    @Transient
    public abstract String getDefaultAuthority();
}
