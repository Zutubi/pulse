package com.zutubi.pulse.master.tove.config.group;

import com.zutubi.tove.annotations.Classification;
import com.zutubi.tove.annotations.Internal;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Transient;

/**
 */
@SymbolicName("zutubi.builtinGroupConfig")
@Internal
@Classification(single = "group")
public class BuiltinGroupConfiguration extends AbstractGroupConfiguration
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
