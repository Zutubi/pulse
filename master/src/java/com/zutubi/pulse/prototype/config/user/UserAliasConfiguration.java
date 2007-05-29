package com.zutubi.pulse.prototype.config.user;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.AbstractConfiguration;

/**
 *
 *
 */
@SymbolicName("internal.userAliasConfig")
public class UserAliasConfiguration extends AbstractConfiguration
{
    private String alias;

    public String getAlias()
    {
        return alias;
    }

    public void setAlias(String alias)
    {
        this.alias = alias;
    }
}
