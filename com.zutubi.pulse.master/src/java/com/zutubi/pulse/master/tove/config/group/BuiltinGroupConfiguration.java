package com.zutubi.pulse.master.tove.config.group;

import com.zutubi.tove.annotations.*;

/**
 */
@Internal
@SymbolicName("zutubi.builtinGroupConfig")
@Classification(single = "group")
@Form(fieldOrder = {"name", "serverPermissions"})
public class BuiltinGroupConfiguration extends GroupConfiguration
{
    @Internal
    private String role;

    public BuiltinGroupConfiguration()
    {
    }

    public BuiltinGroupConfiguration(String name, String role)
    {
        super(name);
        this.role = role;
    }

    @ReadOnly // we do not want people accidentally renaming the built in group names.
    public String getName()
    {
        return super.getName();
    }

    public String getRole()
    {
        return role;
    }

    public void setRole(String role)
    {
        this.role = role;
    }

    @Transient
    public String getDefaultAuthority()
    {
        return role;
    }
}
