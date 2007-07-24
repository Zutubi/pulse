package com.zutubi.pulse.prototype.config.user;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.AbstractConfiguration;

/**
 * An alias is used to match up with a login in an external tool when the
 * user's Pulse login itself does not match.
 */
@SymbolicName("zutubi.userAliasConfig")
public class UserAliasConfiguration extends AbstractConfiguration
{
    private String alias;

    public UserAliasConfiguration()
    {
    }

    public UserAliasConfiguration(String alias)
    {
        this.alias = alias;
    }

    public String getAlias()
    {
        return alias;
    }

    public void setAlias(String alias)
    {
        this.alias = alias;
    }

    public String toString()
    {
        return alias;
    }
}
