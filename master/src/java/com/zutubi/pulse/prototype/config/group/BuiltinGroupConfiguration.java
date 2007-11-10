package com.zutubi.pulse.prototype.config.group;

import com.zutubi.config.annotations.Internal;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Transient;
import com.zutubi.config.annotations.Classification;

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
